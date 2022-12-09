# fsp1-user.ctl
start

###CREATE-FSP-SI
create_si
FSP             #service_type
fsp=fsp1        #name
u   	        #user
4               #version
down
fsp=fsp1
set_peer_id
tester1
set_pp          #set_provision_period
void
void
set_rsp_port_id #set_responder_port
Harness_Port_1
set_rtn_to      #set_return_timeout
60
print
config_completed 

wait            #plain_wait_between_operation_invocations
1

###FSP-BIND
bind
n               #no_configuration_of_bind_op
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-START
start
y               #to_configure
id              #first_pkt_id
1234
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-TRANSFER-DATA
td
y               #to_configure
rn              #radiation_notification
0               #produce_notif
tm              #transmission mode
1               #BD
bl              #blocking
Y               #
psn             #
0               #produce notif.
an              #acknowledge notification
1               #produce notif
mid
1
id
1234
ok
i               #invoke
wait_event      #operation_return
1
1

wait_event      #incoming_async_notify_operation
30
2

###FSP-TRANSFER-DATA
td
y               #to_configure
rn              #radiation_notification
0               #produce_notif
tm              #transmission mode
0               #AD
bl              #blocking
Y               #
psn             #
0               #produce notif.
mid
1
an              #acknowledge notification
0               #produce notif
id
1235
ok
i               #invoke
wait_event      #operation_return
1
1

wait_event      #incoming_async_notify_operation
30
3

wait            #plain_wait_between_operation_invocations
1

###FSP-SCHEDULE-STATUS-REPORT
ssr
y               #to_configure
rrt             #report_request_type
0               #immediate
ok
i               #invoke
wait_event      #operation_return
1
1

wait_event      #incoming_status_report_operation
65
1

###FSP-SCHEDULE-STATUS-REPORT
ssr
y               #to_configure
rrt             #report_request_type
1               #periodic
rc              #reporting cycle
10              #secs
ok
i               #invoke
wait_event      #operation_return
1
1

wait_event      #incoming_status_report_operation
65
2

###FSP-SCHEDULE-STATUS-REPORT
ssr
y               #to_configure
rrt             #report_request_type
2               #stop
rc              #reporting cycle
1
ok
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-STOP
stop
n               #no_configuration_of_stop_op
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-UNBIND
unbind
y               #to_configure
ur              #unbind_reason
0               #0=end
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

###DESTROY-SI
wait            #plain_wait_prior_to_destroy_si
1
up
destroy_si
fsp=fsp1

terminate
shutdown
exit
