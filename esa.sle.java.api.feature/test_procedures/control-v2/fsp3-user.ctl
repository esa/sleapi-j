####start_the_builder
start

####create_a_service_instance
create_si
FSP 	     #service_type
fsp=fsp1   #name
u   	     #user
2

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

###wait_peer_abort
wait_event
65
1

wait
2

####send_a_bind_invoke_operation
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
65
1

wait
2

###peer_abort
peer_abort
y
pad     #diagnostic_code
0       #accessDenied
ok      #send


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
