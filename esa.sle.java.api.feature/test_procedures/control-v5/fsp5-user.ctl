####start_the_builder
start

####create_a_service_instance
create_si
FSP 	     #service_type
fsp=fsp1   #name
u   	     #user
5

####goto_service_instance
down
fsp=fsp1   #name

set_peer_id  #set_peer_identifier
tester1
set_pp       #set_provision_period
2018-09-01T10:00:00
2019-09-01T10:00:00
set_rsp_port_id  #set_responder_port
Harness_Port_1
set_rtn_to   #set_return_timeout
60

####print_service_instance
print

####config_complete_of_service_instance
config_completed 


####send_a_bind_invoke_operation
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
65
1

####send_start_invoke
start
y     #configure_start
id    #first_fsp_id
1234
ok    #end_configuration
i     #invoke 

###wait_for_start_return
wait_event
60
1 

##### send get parameter invocations

###get_parameter
gp
y       #configuration_of_get_parameter_op
al      #apid list
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
btp     # blockingTimeoutPeriod
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
bu      # blocking usage
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
blr     # bit lock required
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
cgv     # clcw global id
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
cpc     # clcw phycical channel
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
ccfr    # cop control frame repetition
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
dm      # delivery mode
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
die     # directive invocation enabled
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
edi     # expected directive id
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
eei     # expected event invocation id
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
esi     # expected Sdiu id
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
fsw     # fop sliding window
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
fs     # fop state
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
ml      # map list
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
mmc     # map mux control
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
mms     # map mux scheme
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
mfl     # maximum frame length
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
mpl     # maximum packet length
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
mrc     # minimum reporting cycle
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
ptm     # permittd transmission mode
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
rc      # reporting cycle
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
rtp     # return timeout period
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
rfar    # rf available required
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
sh      # segement header
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
scfr    # seq control frames repetition
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
teoe    # throw event operation enabled
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
tt      # timeout type
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
ti      # timer initial
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
tl      # transmission limit
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
tfsn    # transmitter frame sequence number
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
vmc     # vc mux control
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
vms     # vc mux scheme
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
vc      # virtual channel
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

###get_parameter
gp
y       #configuration_of_get_parameter_op
dio     # directive invocation online
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
60
1 

####################################


###send_a_stop_invoke_operation
stop
n     #no_configuration_of_stop_op
i     #invoke

###wait_for_stop_return
wait_event
60  #max
1   #1_event 


####send_a_unbind_invoke_operation
unbind
y     #configuration_of_unbind_op
ur    #set_unbind_reason
0     #end
ok    #end_of_config
i     #invoke

###wait_for_unbind_return
wait_event
60  #max
1   #1_event  

####goto_application
up

####destroy_service_instance
destroy_si
fsp=fsp1

####terminate
terminate

####shutdown
shutdown

####end
exit
