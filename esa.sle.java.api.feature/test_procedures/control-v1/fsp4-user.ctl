####start_the_builder
start

####create_a_service_instance
create_si
FSP 	     #service_type
fsp=fsp1   #name
u   	     #user
1

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


####send_a_invoke_directive_operation
dir
y       #configuration
dir     #set-up_directive_type
0       #initADwithoutCLCW
id
1
eid
2
ok
i	#invoke

wait_event
60
1


####send_a_invoke_directive_operation
dir
y       #configuration
dir     #set-up_directive_type
11      #setTimeoutType
id
2
eid
3
ok
i	#invoke

wait_event
60
1

####send_a_throw_event_invoke_operation
te
y	#configuration
id	#event_identifier
33
eid	#event_invoke_id
12344
eq      #event qualifier
123
ok
i	#invoke

wait_event
60
1


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
