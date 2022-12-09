####start_the_builder
start

####create_a_service_instance
create_si
CLTU	     #service_type
cltu=cltu4   #name
u   	     #user
3

####goto_service_instance
down
cltu=cltu4

####configure_the_service_instance
set_peer_id  #set_peer_identifier
tester1
set_pp       #set_provision_period
2018-09-01T10:00:00
2019-09-01T10:00:00

set_rsp_port_id  #set_responder_port
Harness_Port_1
set_rtn_to   #set_return_timeout
80
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

####print_service_instance
print

####config_complete_of_service_instance
config_completed

###wait_debugger
#wait_event
#120
#1


####send_a_bind_invoke_operation
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
blr
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
dm
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
eid
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1




###get_parameter
gp
y     #configuration_of_get_parameter_op
ml
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
mf
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
mi
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
plop
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
rc
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
rto
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
rfa
ok    #end_of_config
i     #invoke

###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
scbr
ok    #end_of_config
i     #invoke


###wait_get_parameter_return
wait_event
65
1

###get_parameter
gp
y     #configuration_of_get_parameter_op
eeid
ok    #end_of_config
i     #invoke


###wait_get_parameter_return
wait_event
65
1



####send_a_unbind_invoke_operation
unbind
y     #configuration_of_unbind_op
ur    #set_unbind_reason
1     #suspend
ok    #end_of_config
i     #invoke

###wait_unbind_return
wait_event
65
1


####goto_application
up

####destroy_service_instance
destroy_si
cltu=cltu4

####terminate
terminate

####shutdown
shutdown

####end
exit
