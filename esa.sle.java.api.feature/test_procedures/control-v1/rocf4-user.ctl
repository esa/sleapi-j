####start_the_builder
start

####create_a_service_instance
create_si
ROCF            #service_type
rocf=onlt4      #name
u            		#user
1

####goto_service_instance
down
rocf=onlt4

####configure_the_service_instance
set_peer_id  #set_peer_identifier
tester1
set_pp       #set_provision_period
2018-09-01T10:00:00
2019-09-01T10:00:00

set_bind_ini  #set_bind_initiative
u
set_rsp_port_id  #set_responder_port
Harness_Port_1
set_rtn_to   #set_return_timeout
600

set_dm   #set_delivery_mode_to_offline_mode
0

####print_service_instance
print

####config_complete_of_service_instance
config_completed


####goto_application
up

####-----------------------------------------
####=======================================================

list_si

####goto_service_instance
down
rocf=onlt4


##############
####SEND_BIND
##############
####send_bind_invoke
bind
n     #no_configuration_of_bind_op
i     #invoke

###wait_bind_return
wait_event
10
1

##############
####SEND_START
##############
####send_start_invoke
start
y    #configure_start
st_#set_start_time
2004-03-01T12:00:00.000
et_#set_end_time
2005-10-01T10:10:10.500
gvc  #set_gvcid
1    #master_channel_VIRTUAL_CHANNEL
1000 #SC_ID
0    #VERSION
5    #VC_ID
cwt  #set_control_word_type
1    #allControlWords
um   #set_update_mode
0    #continuous
ok   #end_configuration
i    #invoke

###wait_for_start_return
wait_event
60
1

###get_parameter
gp
y       #configuration_of_get_parameter_op
bs      #transfer_buffer_size
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
rto     #return_timeout_period
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1   

###get_parameter
gp
y       #configuration_of_get_parameter_op
pgvc    #permitted_global_VCID_set
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

##get_parameter
gp
y       #configuration_of_get_parameter_op
rgvc    #requested_global_VCID
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  


###get_parameter
gp
y       #configuration_of_get_parameter_op
ptcvc   #permitted_TC_VCID_set
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

###get_parameter
gp
y       #configuration_of_get_parameter_op
rtcvc   #requested_TC_VCID
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

###get_parameter
gp
y       #configuration_of_get_parameter_op
pcwt    #permitted_control_word_type_set
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

###get_parameter
gp
y       #configuration_of_get_parameter_op
cwt     #requested_control_word_type
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

###get_parameter
gp
y       #configuration_of_get_parameter_op
pum     #permitted_update_mode_set
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

###get_parameter
gp
y       #configuration_of_get_parameter_op
rum     #requested_update_mode
ok      #end_of_config
i       #invoke

###wait_for_get_parameter_return
wait_event
20
1  

###send_a_stop_invoke_operation
stop
n    #no_CONFIGURATION_FOR_STOP
i    #INVOKE

###wait_for_stop_return
wait_event
30
1

###send_a_unbind_invoke_operation
unbind
y     #configuration_of_unbind_op
ur    #set_unbind_reason
1     #suspend
ok    #end_of_config
i     #invoke

###wait_unbind_return
wait_event
10
1

####goto_application
up

####destroy_service_instance
destroy_si
rocf=onlt4

####terminate
terminate

####shutdown
shutdown

####end
exit
