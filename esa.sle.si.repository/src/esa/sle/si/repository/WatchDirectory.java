package esa.sle.si.repository;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import esa.sle.sicf.si.descriptors.SIDescriptor;
import esa.sle.sicf.si.parser.file.Parser;
import esa.sle.sicf.si.parser.file.ServiceInstanceContainer;

public class WatchDirectory implements Runnable
{

    private static final Logger LOG = Logger.getLogger(WatchDirectory.class.getName());

    private final Map<WatchKey, Path> keys;

    private final WatchService watcher;

    private final SIRepository siRepository;

    private final Path dir;

    private boolean trace = false;

    /**
     * Map containing the file name as key and ServiceIntanceContainer as
     * values. Always needs to be keep updated depending on the changes to the
     * monitored files.
     */
    private final Map<String, ServiceInstanceContainer> mapFileSIC = new HashMap<String, ServiceInstanceContainer>();


    public WatchDirectory(Path dir, SIRepository siRepository) throws IOException
    {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.dir = dir;
        this.siRepository = siRepository;
        this.keys = new HashMap<WatchKey, Path>();
        this.trace = true;
        register(dir);
    }

    private void register(Path dir) throws IOException
    {
        WatchKey key = dir.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        if (this.trace)
        {
            Path prev = this.keys.get(key);
            if (prev == null)
            {
                LOG.log(Level.FINE, "register: ", dir);
            }
            else
            {
                if (!dir.equals(prev))
                {
                    Path[] param = { prev, dir };
                    LOG.log(Level.FINE, "update: ", param);
                }
            }
        }
        this.keys.put(key, dir);
        updateOnFile();
    }

    private void updateOnFile()
    {
        try (Stream<Path> filePathStream = Files.walk(this.dir))
        {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath))
                {
                    processCreationOfFile(filePath, false);
                }
            });
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
    }

    @Override
    public void run()
    {
        for (;;)
        {
            WatchKey key;
            try
            {
                LOG.info("before blocking take take ");
                key = this.watcher.take();
                LOG.info("after blocking take take ");
            }
            catch (InterruptedException x)
            {
                LOG.log(Level.FINE, "InterruptedException detected", x);
                return;
            }

            Path dir = this.keys.get(key);
            if (dir == null)
            {
                System.err.println("WatchKey not recognized!");
                continue;
            }

            key.pollEvents().stream().filter(e -> (e != null)).forEach(e -> processEvents(e, dir));

            boolean valid = key.reset();
            if (!valid)
            {
                this.keys.remove(key);
                if (this.keys.isEmpty())
                {
                    break;
                }
            }
        }
    }

    protected synchronized void processEvents(WatchEvent<?> event, Path dir)
    {

        WatchEvent<Path> ev = cast(event);
        Path nameContext = ev.context();
        Path child = dir.resolve(nameContext);
        String file = child.getFileName().toString();

        String nameEvent = event.kind().name();

        if ((file.charAt(0) != '.') && (file.charAt(file.length() - 1) != '~') && (file.contains(".")))
        {

            if (nameEvent.equals("ENTRY_CREATE"))
            {
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info("ENTRY_CREATE file: " + file);
                }
                processCreationOfFile(child, true);
            }
            if (nameEvent.equals("ENTRY_DELETE"))
            {
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info("ENTRY_DELETE file: " + file);
                }
                processDeletionOfFile(child);
            }
            if (nameEvent.equals("ENTRY_MODIFY"))
            {
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info("ENTRY_MODIFY file: " + file);
                }
                processModificationOfFile(child);
            }
        }
    }

    /**
     * Process the modification of the file. If a file is modified (i.e. a
     * service instance within the file are created or deleted, but the file
     * itself is not deleted) the file is parsed again and the new/ modified
     * instances are considered. Union of the new and old si list minus the
     * intersection = only the modified/ new elements.
     * 
     * @param child Path to the file that was modified
     */

    private void processModificationOfFile(Path child)
    {
        String key = child.getFileName().toString();
        if (this.mapFileSIC.containsKey(key))
        {
            Parser parser = new Parser(child.toString());
            parser.parseFile();
            ServiceInstanceContainer valueNew = parser.getServInstanceContainer();
            ServiceInstanceContainer valueOld = this.mapFileSIC.get(key);

            List<SIDescriptor> olds = valueOld.getSIDescriptionList();
            List<SIDescriptor> news = valueNew.getSIDescriptionList();

            processDeletionOfSIDescriptors(news, olds, key);
            processCreationOfSIDescriptors(news, olds, key);
            processModificationOfSIDescriptors(news, olds, key);

        }
        else
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info("on modification the key - file name doesn't exist");
            }
        }
    }

    /**
     * Steps: 1. Get a list of all new and different Service Instance
     * descriptors that are in the new list and not in the old list. 2. Get a
     * list of only new (created) elements only. 3. Get a list of only modified
     * elements (i.e. si descriptors that are present in both lists but modified
     * in the new one) 4. For each only modified element and for each siLoader
     * call the onServiceUpdated. 5. Keep global map updated.
     * 
     * @param news
     * @param olds
     * @param loadersLocal
     * @param key
     */
    private void processModificationOfSIDescriptors(List<SIDescriptor> news, List<SIDescriptor> olds, String key)
    {
        List<SIDescriptor> newDifferentSIdescriptors = news.stream().filter(n -> !olds.contains(n))
                .collect(Collectors.toList());
        List<SIDescriptor> createdSIdescriptors = news.stream().filter(n -> !containsById(olds, n))
                .collect(Collectors.toList());
        List<SIDescriptor> onlyModified = newDifferentSIdescriptors.stream()
                .filter(n -> !containsById(createdSIdescriptors, n)).collect(Collectors.toList());
        ServiceInstanceContainer value = this.mapFileSIC.get(key);
        List<SIDescriptor> oldListOfValue = value.getSIDescriptionList();
        List<SIDescriptor> aux = oldListOfValue.stream().filter(n -> containsById(onlyModified, n))
                .collect(Collectors.toList());
        oldListOfValue.removeAll(aux);
        oldListOfValue.addAll(onlyModified);
        value.setSIDescription(oldListOfValue);
        this.mapFileSIC.put(key, value);

        this.siRepository.notifySiUpdated(onlyModified);
    }

    /**
     * Steps: 1. Get all the new created Service Instance descriptors (elements)
     * i.e. the elements that are in the new list but not in the old list. 2.
     * For each element that has been created and for each siLoader call the
     * onServiceAdded. 3. Update the siContainer map, i.e. the list of
     * descriptors of the value from the siContainers map.
     * 
     * @param news
     * @param olds
     * @param loadersLocal
     * @param key
     */
    private void processCreationOfSIDescriptors(List<SIDescriptor> news, List<SIDescriptor> olds, String key)
    {
        List<SIDescriptor> createdSIdescriptors = news.stream().filter(n -> !containsById(olds, n))
                .collect(Collectors.toList());

        ServiceInstanceContainer value = new ServiceInstanceContainer();
        value = this.mapFileSIC.get(key);
        List<SIDescriptor> oldListOfValue = value.getSIDescriptionList();
        List<SIDescriptor> newListOfValue = new ArrayList<SIDescriptor>();
        newListOfValue = oldListOfValue;
        for (SIDescriptor si : createdSIdescriptors)
        {
            newListOfValue.add(si);
        }
        value.setSIDescription(newListOfValue);
        this.mapFileSIC.put(key, value);

        this.siRepository.notifySiAdded(createdSIdescriptors);
    }

    /**
     * Steps: 1. Get all the deleted Service Instance descriptors (elements)
     * i.e. the elements that are in the old list but not in the new list. 2.
     * For each element that has been deleted and for each siLoader call the
     * onRemove. 3. Update the siContainer map, i.e. the list of descriptors of
     * the value from the siContainers map.
     * 
     * @param news
     * @param olds
     * @param loadersLocal
     * @param key
     */
    private void processDeletionOfSIDescriptors(List<SIDescriptor> news, List<SIDescriptor> olds, String key)
    {
        List<SIDescriptor> deletedSIdescriptors = olds.stream().filter(n -> !containsById(news, n))
                .collect(Collectors.toList());

        ServiceInstanceContainer value = new ServiceInstanceContainer();
        value = this.mapFileSIC.get(key);
        List<SIDescriptor> oldListOfValue = value.getSIDescriptionList();
        List<SIDescriptor> newListOfValue = oldListOfValue.stream()
                .filter(el -> !containsById(deletedSIdescriptors, el)).collect(Collectors.toList());
        value.setSIDescription(newListOfValue);
        this.mapFileSIC.put(key, value);

        this.siRepository.notifySiRemoved(deletedSIdescriptors);
    }

    /**
     * Return true if there exists one element x in news such that x identifier
     * is equal with n identifier
     * 
     * @param news
     * @param n
     * @return
     */
    private boolean containsById(List<SIDescriptor> siDescriptorList, SIDescriptor n)
    {
        String idn = n.getServiceInstanceId().getAsciiForm();
        List<SIDescriptor> list = siDescriptorList.stream()
                .filter(el -> el.getServiceInstanceId().getAsciiForm().equals(idn)).collect(Collectors.toList());
        if (list.size() > 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Searches in siContainers for the key (file that was deleted). In case the
     * file that was just deleted exists in the map, then it is deleted.
     * 
     * @param child path to the file that was deleted
     */
    private void processDeletionOfFile(Path child)
    {
        String key = child.getFileName().toString();
        ServiceInstanceContainer value = new ServiceInstanceContainer();

        value = this.mapFileSIC.get(key);
        if (value != null)
        {
            this.siRepository.notifySiRemoved(value.getSIDescriptionList());
            this.mapFileSIC.entrySet().removeIf(e -> e.getKey().equals(key));
        }

    }

    /**
     * Process the creation of a new file.
     * 
     * @param child path to the folder file
     */
    private void processCreationOfFile(Path child, boolean notify)
    {
        String key = child.getFileName().toString();
        if (!this.mapFileSIC.containsKey(key))
        {
            Parser parser = new Parser(child.toString());
            parser.parseFile();
            ServiceInstanceContainer value = new ServiceInstanceContainer();
            value = parser.getServInstanceContainer();
            this.mapFileSIC.put(key, value);
            if (notify)
            {
                this.siRepository.notifySiAdded(value.getSIDescriptionList());
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event)
    {
        return (WatchEvent<T>) event;
    }

    public synchronized List<SIDescriptor> getServiceInstanceDescriptors()
    {
        List<SIDescriptor> toReturn = new ArrayList<SIDescriptor>();
        for (ServiceInstanceContainer sic : this.mapFileSIC.values())
        {
            toReturn.addAll(sic.getSIDescriptionList());
        }
        return toReturn;
    }

}
