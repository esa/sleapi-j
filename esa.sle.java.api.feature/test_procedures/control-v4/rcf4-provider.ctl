####start_the_builder
start

####create_a_service_instance
create_si
RCF          #service_type
rcf=onlt1         #name
p            #provider
4

####goto_service_instance
down
rcf=onlt1

####configure_the_service_instance
set_peer_id  #set_peer_identifier
tester1
set_pp       #set_provision_period
2018-09-01T10:00:00
2019-09-01T10:00:00

set_bind_ini  #set_bind_initiative
u
set_rsp_port_id  #set_responder_port
Harness_Port_1
set_rtn_to   #set_return_timeout
60
set_dm   #set_delivery_mode_to_online_complete
1
set_ll   #set_latency_limit
20
set_buffer_size  #set_buffer_size
2
set_init_ps   #set_initial_production_status
0
set_init_fsl   #set_initial_frame_sync_lock_to_inlock
0
set_init_cdml   #set_initial_carrier_demod_lock_to_inlock
0
set_init_scdl   #set_initial_subcarrier_demod_lock_to_inlock
0
set_init_ssl   #set_initial_symbol_sync_lock_to_inlock
0
set_ps   #set_production_status_to_running
0

set_pgvcid	#set_permitted_list_GVCID_list
4		#4_GVCID
0		#master
33		#SCID
0		#version
0		#vcid
0		#master
46		#SCID
1		#version
0		#vcid
1		#virtual
1000		#SCID
0		#version
5		#vcid
1		#virtual
1000		#SCID
0		#version
6		#vcid

####print_service_instance
print

####config_complete_of_service_instance
config_completed

###wait_bind_invoke
wait_event
60
1
y

###wait_start_invoke
wait_event
60
1
y


###wait_stop_invoke
wait_event
180
1
y

###wait_unbind_invoke
wait_event
60
1
y

###end
wait_event
2
1

####goto_application
up

####destroy_service_instance
destroy_si
rcf=onlt1

####terminate
terminate

####shutdown
shutdown

####end
exit

