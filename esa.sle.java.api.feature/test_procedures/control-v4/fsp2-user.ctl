# fsp2-user.ctl
start

###CREATE-FSP-SI
create_si
FSP             #service_type
fsp=fsp2        #name
u   	        #user
4               #version
down
fsp=fsp2
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

###FSP-GET-PARAMETER mapList 16
gp
y               #to_configure
ml	            #mapList
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER mapMuxControl 17
gp
y               #to_configure
mmc	            #mapMuxControl
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER mapMuxScheme 18
gp
y               #to_configure
mms	            #mapMuxScheme
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER maximumFrameLength 19
gp
y               #to_configure
mfl	            #maximumFrameLength
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER maximumPacketLength 20
gp
y               #to_configure
mpl             #maximumPacketLength
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER permittedTransmissionMode 107
gp
y               #to_configure
ptm	            #permittedTransmissionMode
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER reportingCycle 26
gp
y               #to_configure
rc	            #reportingCycle
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER returnTimeoutPeriod 29
gp
y               #to_configure
rtp	            #returnTimeoutPeriod
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER segmentHeader 32
gp
y               #to_configure
sh	            #segmentHeader
ok              #end_of_config
i               #invoke
wait_event      #operation_return
1
1

wait            #plain_wait_between_operation_invocations
1

###FSP-GET-PARAMETER timeoutType 35
gp
y               #to_configure
tt	            #timeoutType
ok              #end_of_config
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
fsp=fsp2

terminate
shutdown
exit
