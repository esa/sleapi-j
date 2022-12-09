####start_the_builder
start

####create_a_service_instance
create_si
RAF          #service_type
raf=onlt1         #name
u            #user
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

set_rsp_port_id  #set_responder_port
Harness_Port_1
set_rtn_to   #set_return_timeout
60

####print_service_instance
print

####config_complete_of_service_instance
config_completed

####send_bind_invoke
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
60
1

###wait_peer_abort
wait_event
65
1

####send_a_bind_invoke_operation
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
65
1

###peer_abort
peer_abort
y
pad     #diagnostic_code
0       #accessDenied
ok      #send


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

####end
exit
