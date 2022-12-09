####start_the_builder
start

####create_a_service_instance
use_si
CLTU
# SI Identifier (sagr=[SAGR].spack=[SPACK].fsl-fg=[FSL-FG].[sle-protocol]=[id]):
sagr=3.spack=facility-PASS1.fsl-fg=1.cltu=cltu1
p
2

####goto_service_instance
down
cltu=cltu1

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
set_rtn_to   	#set_return_timeout
60
set_blr		#set_bit_lock_required
y		#yes
set_maxl	#set_max_sldu_length
512
set_mf		#set_modulation_frequency
112
set_mi		#set_modulation_index
2
set_plop	#set_plop_in_effect
1		#cltuPIE_plop1
set_rfr		#set_rf_lock_required
y
set_scbrr	#set_subcarr_to_bitrate_ratio
221
set_mbs		#set_max_buffer_size
1024
set_init_ps	#set_initial_production_status
0		#operational
set_init_uls	#set_initial_uplink_status
3		#nominal
set_nm
0


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
y	#return
spd	#setup_start_production_time
2018-02-27T10:00:00
epd     #setup_end_production_time
2018-08-01T11:00:00
ok	#end_config


start_loop_sequence

###wait_transfer_data
wait_event
100
1
y	#no_return
y	#store

###async_notify_cltu_radiated
an
y	#setup
nt      #notification_type
0       #cltu_radiated
idlp    #id_last_processed
1
idok    #id_last_ok
1
rst	#setup_radiation_start_time
2018-02-23T14:24:20.200
ret	#setup_radiation_stop_time
2018-08-23T14:34:20.201
cs	#cltu_status
0	#radiated
ps      #production_status
0       #operational
us      #uplink_status
3       #nominal
ok

end_loop_sequence

play_loop_sequence
2


###wait_on_stop_invoke
wait_event
30
1
y


###wait_unbind_invoke
wait_event
30
1
y

####goto_application
up

####destroy_service_instance
destroy_si
cltu=cltu1

####terminate
terminate

####shutdown
shutdown

exit
