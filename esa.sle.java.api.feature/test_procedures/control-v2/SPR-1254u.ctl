####start_the_builder
start

#################################################
####create_a_service_instance
#################################################
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
200

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
u            #user
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
set_rsp_port_id  #set_responder_port
Harness_Port_2
set_rtn_to   #set_return_timeout
200

####print_service_instance
print

####config_complete_of_service_instance
config_completed

#################################################
####send_bind_on_first_EXP_OK
#################################################
up
down
raf=onlt1

####send_a_bind_invoke_operation
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
60
1

#################################################
####send_bind_on_second_EXP_ERR
#################################################
up
down
raf=onlt2

####send_a_bind_invoke_operation
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
50
1

#################################################
####reload_cs_configuration_and_bring_up_second_service
#################################################
up
down
raf=onlt1

####wait_for_any_event_for_70_secs
wait_event
70
1

#################################################
####send_bind_on_second_EXP_OK
#################################################
up
down
raf=onlt2

####send_a_bind_invoke_operation
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
60
1

#################################################
####send_start_stop_port_1_EXP_OK
#################################################
up
down
raf=onlt1

####send_start_invoke
start
y    #configure_start
st   #set_start_time
2016-02-04T10:10:10.500
et   #set_end_time
2016-12-02T10:10:10.500
fq   #set_requested_frame_quality
0    #good
ok    #end_configuration
i     #invoke

###wait_for_start_return
wait_event
5
1

##wait_10s
wait
10

###send_stop_invoke
stop
n     #no_configuration_of_stop_op
i     #invoke

###wait_for_stop_return
wait_event
5
1

#################################################
####send_unbind_on_first_EXP_OK
#################################################

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

print 

###end
wait_event
10
1

#################################################
####send_unbind_on_second_EXP_OK
#################################################
up
down
raf=onlt2

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

print

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

####terminate
terminate

####shutdown
shutdown

####end
exit
