# fsp4-user.ctl
start

###CREATE-FSP-SI
create_si
FSP             #service_type
fsp=fsp4        #name
u   	        #user
4               #version
down
fsp=fsp4
set_peer_id  	#set_peer_identifier
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

####FSP-INVOKE-DIRECTIVE
dir
y               #to_configure
dir             #set-up_directive_type
0               #initADwithoutCLCW
id
1
eid
2
ok
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

####FSP-INVOKE-DIRECTIVE
dir
y               #to_configure
dir             #set-up_directive_type
11              #setTimeoutType
id
2
eid
3
ok
i               #invoke
wait_event      #operation_return
1
1

####FSP-THROW-EVENT
te
y	            #configuration
id	            #event_identifier
33
eid            #event_invoke_id
12344
eq             #event qualifier
123
ok
i              #invoke

wait_event
60
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
fsp=fsp4

terminate
shutdown
exit
