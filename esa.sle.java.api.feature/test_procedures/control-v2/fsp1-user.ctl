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
y     #configure_start
id    #first_fsp_id
1234
ok    #end_configuration
i     #invoke 

###wait_for_start_return
wait_event
60
1

wait
2

####send_a_transfer_data_invoke_operation
td
y	#configuration
rn	#radiation_notification
0	#produce_notif
tm      #transmission mode
1       #BD
bl      #blocking
Y       #
psn     #
0       #produce notif.
an      #acknowledge notification
1       #produce notif
mid
1
id
1234
ok
i	#invoke


###wait_for_td_returns
wait_event
30
1

###wait_for_an
wait_event
30
2


####send_a_transfer_data_invoke_operation
td
y	#configuration
rn	#radiation_notification
0	#produce_notif
tm      #transmission mode
0       #AD
bl      #blocking
Y       #
psn     #
0       #produce notif.
mid
1
an      #acknowledge notification
0       #produce notif
id
1235
ok
i	#invoke


###wait_for_td_returns
wait_event
30
1

###wait_for_an
wait_event
30
3

wait
2

####send_a_immediate_ssr_invoke_operation
ssr
y	#configuration
rrt	#report_request_type
0	#immediatly
ok
i	#invoke

###wait_ssr_return
wait_event
65
1

###wait_status_report
wait_event
65
1

####send_a_periodic_ssr_invoke_operation
ssr
y	#configuration
rrt	#report_request_type
1	#periodic
rc      #reporting cycle
10
ok
i	#invoke

###wait_ssr_return
wait_event
65
1

###wait_ssr_return
wait_event
65
2

####send_a_stop_ssr_invoke_operation
ssr
y	#configuration
rrt	#report_request_type
2	#immediatly
rc      #reporting cycle
1
ok
i	#invoke

###wait_ssr_return
wait_event
65
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
