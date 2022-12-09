####start_the_builder
start

#################################################
####create_a_service_instance_EXT_OK
#################################################
create_si
RAF          #service_type
raf=onlt1         #name
p            #provider
2

####goto_service_instance
down
raf=onlt1

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
set_dm   #set_delivery_mode_to_online_timely
0
set_ll   #set_latency_limit
10
set_buffer_size  #set_buffer_size
10
set_init_ps   #set_initial_production_status
1
set_init_fsl   #set_initial_frame_sync_lock_to_inlock
2
set_init_cdml   #set_initial_carrier_demod_lock_to_inlock
1
set_init_scdl   #set_initial_subcarrier_demod_lock_to_inlock
3
set_init_ssl   #set_initial_symbol_sync_lock_to_inlock
1
set_ps   #set_production_status_to_running
1

####print_service_instance
print

####config_complete_of_service_instance
config_completed

#################################################
####create_a_service_instance_port_2
#################################################
up

create_si
RAF          #service_type
raf=onlt2         #name
p            #provider
2

####goto_service_instance
down
raf=onlt2

####configure_the_service_instance
set_peer_id  #set_peer_identifier
tester1
set_pp       #set_provision_period
2016-01-01T10:10:10.500
2017-01-01T10:20:20.200
set_bind_ini  #set_bind_initiative
u
set_rsp_port_id  #set_responder_port
Harness_Port_2
set_rtn_to   #set_return_timeout
60
set_dm   #set_delivery_mode_to_online_timely
0
set_ll   #set_latency_limit
10
set_buffer_size  #set_buffer_size
10
set_init_ps   #set_initial_production_status
1
set_init_fsl   #set_initial_frame_sync_lock_to_inlock
2
set_init_cdml   #set_initial_carrier_demod_lock_to_inlock
1
set_init_scdl   #set_initial_subcarrier_demod_lock_to_inlock
3
set_init_ssl   #set_initial_symbol_sync_lock_to_inlock
1
set_ps   #set_production_status_to_running
1

####print_service_instance
print

#################################################
####config_completed_port_2_EXT_ERR
#################################################

####config_complete_of_service_instance
config_completed

up

####destroy_service_instance_2
destroy_si
raf=onlt2

#################################################
####wait_bind_on_first_EXP_OK
#################################################
down
raf=onlt1

###wait_bind_invoke
wait_event
60
1
y

#################################################
####reload_cs_configuration_and_bring_up_second_service
#################################################
up
down
raf=onlt1

####wait_for_any_event_for_60_secs
wait_event
60
1

#################################################
####create_a_service_instance_port_2
#################################################
up

create_si
RAF          #service_type
raf=onlt2         #name
p            #provider
2

####goto_service_instance
down
raf=onlt2

####configure_the_service_instance
set_peer_id  #set_peer_identifier
tester1
set_pp       #set_provision_period
2016-01-01T10:10:10.500
2017-01-01T10:20:20.200
set_bind_ini  #set_bind_initiative
u
set_rsp_port_id  #set_responder_port
Harness_Port_2
set_rtn_to   #set_return_timeout
60
set_dm   #set_delivery_mode_to_online_timely
0
set_ll   #set_latency_limit
10
set_buffer_size  #set_buffer_size
10
set_init_ps   #set_initial_production_status
1
set_init_fsl   #set_initial_frame_sync_lock_to_inlock
2
set_init_cdml   #set_initial_carrier_demod_lock_to_inlock
1
set_init_scdl   #set_initial_subcarrier_demod_lock_to_inlock
3
set_init_ssl   #set_initial_symbol_sync_lock_to_inlock
1
set_ps   #set_production_status_to_running
1

####print_service_instance
print

#################################################
####config_completed_port_2_EXT_OK
#################################################

####config_complete_of_service_instance
config_completed

#################################################
####wait_bind_on_second_EXP_OK
#################################################
up
down
raf=onlt2

###wait_bind_invoke
wait_event
60
1
y

#################################################
####wait_start_stop_port_1_EXP_OK
#################################################
up
down
raf=onlt1

###wait_start_invoke
wait_event
60
1
y

###wait_stop_invoke
wait_event
60
1
y

#################################################
####wait_unbind_on_first_EXP_OK
#################################################

###wait_unbind_invoke
wait_event
60
1
y

###end
wait_event
5
1

#################################################
####wait_unbind_on_second_EXP_OK
#################################################
up
down
raf=onlt2

###wait_unbind_invoke
wait_event
60
1
y

#################################################
####clean_up
#################################################

###end
wait_event
5
1

####goto_application
up

####destroy_service_instance_1
destroy_si
raf=onlt1

####destroy_service_instance_2
destroy_si
raf=onlt2

#################################################
####create_a_service_instance_EXT_ERR
#################################################
create_si
RAF          #service_type
raf=onlt1         #name
p            #provider
2

####goto_service_instance
down
raf=onlt1

####configure_the_service_instance
set_peer_id  #set_peer_identifier
tester1
set_pp       #set_provision_period
2016-01-01T10:10:10.500
2017-01-01T10:20:20.200
set_bind_ini  #set_bind_initiative
u
set_rsp_port_id  #set_responder_port
Harness_Port_1
set_rtn_to   #set_return_timeout
60
set_dm   #set_delivery_mode_to_online_timely
0
set_ll   #set_latency_limit
10
set_buffer_size  #set_buffer_size
10
set_init_ps   #set_initial_production_status
1
set_init_fsl   #set_initial_frame_sync_lock_to_inlock
2
set_init_cdml   #set_initial_carrier_demod_lock_to_inlock
1
set_init_scdl   #set_initial_subcarrier_demod_lock_to_inlock
3
set_init_ssl   #set_initial_symbol_sync_lock_to_inlock
1
set_ps   #set_production_status_to_running
1

####print_service_instance
print

####config_complete_of_service_instance
config_completed

up

####destroy_service_instance_1
destroy_si
raf=onlt1

#################################################
####exit
#################################################

####terminate
terminate

####shutdown
shutdown

####end
exit
