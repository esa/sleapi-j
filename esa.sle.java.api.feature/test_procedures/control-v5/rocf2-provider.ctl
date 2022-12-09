####start_the_builder
start

####create_a_service_instance
create_si
ROCF         #service_type
rocf=onlt2   #name
p            #provider
5

####goto_service_instance
down
rocf=onlt2

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

set_pcwts #set_permitted_control_word_type_list
0

set_ptcvcid #set_permitted_TC_VcId_list
0

set_pums #set_permitted_update_mode_list
0

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

###send_sync_notify
sn
y       #configuration_of_sync_notify
ps      #production_status_change
0       #running
ok

wait_event
1
1

###send_sync_notify
sn
y       #configuration_of_sync_notify
ps      #production_status_change
1       #interrupted
ok

wait_event
1
1

###send_sync_notify
sn
y       #configuration_of_sync_notify
ps      #production_status_change
2       #halted
ok

wait_event
1
1

###send_sync_notify
sn
y       #configuration_of_sync_notify
lfs     #loss_of_frame_synchronization
0       #in_lock
ok

###send_sync_notify
sn
y       #configuration_of_sync_notify
lfs     #loss_of_frame_synchronization
1       #out_of_lock
ok


###send_sync_notify
sn
y       #configuration_of_sync_notify
dd      #data_discarded
ok

wait_event
1
1

###send_sync_notify
sn
y       #configuration_of_sync_notify
eod     #end_of_data
ok

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
wait
3

####goto_application
up

####destroy_service_instance
destroy_si
rocf=onlt2

####terminate
terminate

####shutdown
shutdown

####end
exit

