# fsp2-user.ctl
start

###CREATE-FSP-SI
create_si
FSP             #service_type
fsp=fsp2        #name
u   	        #user
5               #version
down
fsp=fsp2
set_peer_id  	#set_peer_identifier
tester1
set_pp          #set_provision_period
2018-09-05T00:00:00
2019-09-05T00:00:00
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
5
1

###FSP-START
start
y               #to_configure
id              #first_pkt_id
1234
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER mapList 16
gp
y               #to_configure
ml	            #mapList
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER mapMuxControl 17
gp
y               #to_configure
mmc	            #mapMuxControl
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER mapMuxScheme 18
gp
y               #to_configure
mms	            #mapMuxScheme
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER maximumFrameLength 19
gp
y               #to_configure
mfl	            #maximumFrameLength
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER maximumPacketLength 20
gp
y               #to_configure
mpl             #maximumPacketLength
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER permittedTransmissionMode 107
gp
y               #to_configure
ptm	            #permittedTransmissionMode
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-GET-PARAMETER reportingCycle 26
gp
y               #to_configure
rc	            #reportingCycle
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-GET-PARAMETER returnTimeoutPeriod 29
gp
y               #to_configure
rtp	            #returnTimeoutPeriod
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-GET-PARAMETER segmentHeader 32
gp
y               #to_configure
sh	            #segmentHeader
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-GET-PARAMETER timeoutType 35
gp
y               #to_configure
tt	            #timeoutType


ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER clcwGlobalVcid 202
gp
y               #to_configure
cgv	            #clcwGlobalVcid
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-GET-PARAMETER clcwPhysicalChannel 203
gp
y               #to_configure
cpc	            #clcwPhysicalChannel
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-GET-PARAMETER copCntrFramesRepetition 300
gp
y               #to_configure
ccfr            #copCntrFramesRepetition
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-GET-PARAMETER minReportingCycle 301
gp
y               #to_configure
mrc	            #minReportingCycle
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER sequCntrFramesRepetition 303
gp
y               #to_configure
scfr            #sequCntrFramesRepetition
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1

###FSP-GET-PARAMETER throwEventOperationEnabled 304
gp
y               #to_configure
teoe            #throwEventOperation
ok              #end_of_config
i               #invoke
wait_event      #operation_return
5
1


###FSP-STOP
stop
n               #no_configuration_of_stop_op
i               #invoke

wait_event      #wait_for_stop_return
60              #max
1               #1_event 

###FSP-UNBIND
unbind
y               #to_configure
ur              #unbind_reason
0               #0=end
ok              #end_of_config
i               #invoke

wait_event      #wait_for_unbind_return
60              #max
1               #1_event 

###DESTROY-SI
up
destroy_si
fsp=fsp2

terminate
shutdown
exit
