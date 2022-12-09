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

####send_start_invoke
start
y    #configure_start
st   #set_start_time
2018-09-01T10:42:00.500
et   #set_end_time
2018-09-01T11:42:00.500
fq   #set_requested_frame_quality
0    #good
ok    #end_configuration
i     #invoke

###wait_for_start_return
wait_event
65  #max
1   #1_event


###schedule_status_report
ssr
y       #configure_the_operation
rrt     #report_type_request
0       #immediately
ok      #config_completed
i       #invoke

###wait_return_operation
wait_event
60  
1   

###wait_status_report
wait_event
40  
1   

###schedule_status_report
ssr
y       #configure_the_operation
rrt     #report_type_request
1       #periodic
rc      #reporting_cycle
10
ok      #config_completed
i       #invoke

###wait_return_operation
wait_event
60  
1   


###wait_1_status_report
wait_event
40  
1   

###stop_scheduled_status_report
ssr
y       #configure_the_operation
rrt     #report_type_request
2       #stop
ok      #config_completed
i       #invoke

###wait_return_operation
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
