# fsp3-user.ctl
start

###CREATE-FSP-SI
create_si
FSP             #service_type
fsp=fsp3        #name
u   	        #user
4               #version
down
fsp=fsp3
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

wait_event      #incoming_peer_abort_operation
65
1

wait            #plain_wait_between_operation_invocations
2

###FSP-BIND
bind
n               #no_configuration_of_bind_op
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
2

###FSP-PEER-ABORT BY USER
peer_abort
y               #to_configure
pad             #diagnostic_code
0               #accessDenied
ok              #end_of_config

###DESTROY-SI
wait            #plain_wait_prior_to_destroy_si
1
up
destroy_si
fsp=fsp3

terminate
shutdown
exit
