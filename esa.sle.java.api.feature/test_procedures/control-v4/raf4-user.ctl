####start_the_builder
start

####create_a_service_instance
create_si
RAF          #service_type
raf=onlt1         #name
u            #user
4

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

####send_start_invoke
start
y    #configure_start
st   #set_start_time
2018-09-01T10:42:00.500
et   #set_end_time
2018-09-01T11:42:00.500
fq   #set_requested_frame_quality
1    #erred
ok    #end_configuration
i     #invoke

###wait_for_start_return
wait_event
65  #max
1   #1_event

###get_parameter
gp
y       #configuration_of_get_parameter_op
bs      #buffer_size
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1   

###get_parameter
gp
y       #configuration_of_get_parameter_op
dm      #delivery_mode
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1   

###get_parameter
gp
y       #configuration_of_get_parameter_op
ll      #latency_limit
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1   

###get_parameter
gp
y       #configuration_of_get_parameter_op
rc      #reporting_cycle
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1   

###get_parameter
gp
y       #configuration_of_get_parameter_op
rfq     #requested_frame_quality
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

###get_parameter
gp
y       #configuration_of_get_parameter_op
rto     #return_timeout_period
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
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
