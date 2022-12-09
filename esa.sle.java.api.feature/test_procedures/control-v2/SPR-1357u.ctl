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

####send_a_bind_invoke_operation
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
2009-02-04T10:10:10.500
et   #set_end_time
2010-12-02T10:10:10.500
fq   #set_requested_frame_quality
0    #good
ok    #end_configuration
i     #invoke

###wait_for_start_return
wait_event
5
1

###wait_td_good
wait_event
15
1

###wait_td_good
wait_event
15
1

###wait_td_good
wait_event
15
1

###wait_sync_notify
wait_event
5
1

##wait_11s
wait
11

###send_stop_invoke
stop
n     #no_configuration_of_stop_op
i     #invoke

###wait_for_stop_return
wait_event
5
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
5
1

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
