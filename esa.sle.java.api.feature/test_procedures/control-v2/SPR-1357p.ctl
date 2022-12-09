####start_the_builder
start

####create_a_service_instance
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

###send_transfer_data
td
y     ##config_the_op
fq    ##frame_quality
0     ##good
dl
256
ok

###send_transfer_data
td
y     ##config_the_op
fq    ##frame_quality
1     ##erred
dl
256
ok

###send_transfer_data
td
y     ##config_the_op
fq    ##frame_quality
2     ##undetermined
dl
256
ok

###send_sync_notify
sn
y     ##config_the_op
ps    ##production_status
0     ##running
eod   ##end_of_data_notification
lfs   ##loss_frame_sync
1
ok

###wait_10s_for_latency_limit
wait
10

###wait_stop_invoke
wait_event
60
1
y

###wait_unbind_invoke
wait_event
60
1
y

###end
wait_event
5
1

####goto_application
up

####destroy_service_instance
destroy_si
raf=onlt1

####terminate
terminate

####shutdown
shutdown


exit
