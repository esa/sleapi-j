####start_the_builder
start

####create_a_service_instance
create_si
FSP          #service_type
fsp=fsp1     #name
p            #provider
2

####goto_service_instance
down
fsp=fsp1


####configure_the_service_instance
set_peer_id  	#set_peer_identifier
tester1
set_pp       	#set_provision_period
2018-09-01T10:00:00
2019-09-01T10:00:00
set_bind_ini  	#set_bind_initiative
u
set_rsp_port_id	#set_responder_port
Harness_Port_1
set_mbs
4711

set_mpl
28000

set_mfl
1024

set_init_ps
2

set_apidl
7,8,9

set_blr
n

set_bto
200

set_bu
1

set_die
n

set_init_dio
y

set_mapl
2,3,4,1

set_rfar
n

set_shp
y

set_vcms
1

set_vcpv
0,1

set_vcpril
0,20:1,30

set_vc
1

set_ptm
1

set_cgv
1
255
1
62

set_cpc
Ka-band

set_mrc
3

set_scfr
5

set_ccfr
6

set_teoe
Y

set_rl
58

set_slw
3

set_tot
1

set_tinit
1000


set_rtn_to   	#set_return_timeout
60
dir_online
n

set_fops
0
set_tfsc
1
set_trl
1
set_muxs
0 

###set_production_status_operationalBD
set_ps
1       #operationalBD
0       #fop alert(no)
1024    #bufferSize
y       #notify
NULL

###set_production_status_operationalADandBD
set_ps
2       #operationalADandBD
0       #fop alert(no)
1024    #bufferSize
y       #notify
NULL

####print_service_instance
print

####config_complete_of_service_instance
config_completed


###wait_bind_invoke
wait_event
300
1
y

###wait_start_invoke
wait_event
60
1
y       #return
spd     #setup_start_production_time
2009-08-22T14:20:20.200
id      #first_packetid_the_provider_shall_accept
0
ok      #end_config
 
## here the SLE API handles the get parameter invocations 

###wait_stop_invocation
wait_event
60
1
y

###wait_unbind_invocation
wait_event
60
1
y

wait
3

####goto_application
up

####destroy_service_instance
destroy_si
fsp=fsp1

####terminate
terminate

####shutdown
shutdown

exit
