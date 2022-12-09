####start_the_builder
start

####create_a_service_instance
create_si
FSP          #service_type
fsp=fsp1     #name
p            #provider
2

####goto_service_instance
down
fsp=fsp1


####configure_the_service_instance
set_peer_id  	#set_peer_identifier
tester1
set_pp       	#set_provision_period
2018-09-01T10:00:00
2019-09-01T10:00:00
set_bind_ini  	#set_bind_initiative
u
set_rsp_port_id	#set_responder_port
Harness_Port_1
set_rtn_to   	#set_return_timeout
60
dir_online
n
set_init_ps
0
set_vcms
0
set_fops
0
set_tfsc
1
set_trl
1
set_tinit
1000
set_tot
0
set_slw
3
set_bu
0
set_mbs
60000
set_shp
y
set_bto
1000
set_apidl
1,2,3,4
set_mpl
500
set_die
y
set_mapl
2,3,4,1
set_muxs
0 
set_ptm
2
set_mfl
123

###set_production_status_operationalBD
set_ps
1       #operationalBD
0       #fop alert(no)
1024    #bufferSize
y       #notify
NULL

###set_production_status_operationalADandBD
set_ps
2       #operationalADandBD
0       #fop alert(no)
1024    #bufferSize
y       #notify
NULL

####print_service_instance
print

####config_complete_of_service_instance
config_completed


###wait_bind_invoke
wait_event
300
1
y

###wait_start_invoke
wait_event
60
1
y       #return
spd     #setup_start_production_time
2009-08-22T14:20:20.200
id      #first_packetid_the_provider_shall_accept
0
ok      #end_config
 
 
###wait_invoke_directive_invocation
wait_event
60
1
r
eid
2
diag
1
ok 

###wait_invoke_directive_invocation
wait_event
60
1
r
eid
3
ok 

###wait_throw_event_invocation
wait_event
60
1
r    #setup_and_send_return
eeid #expected_id
12345
ok 

###wait_stop_invocation
wait_event
60
1
y

###wait_unbind_invocation
wait_event
60
1
y

wait
3

####goto_application
up

####destroy_service_instance
destroy_si
fsp=fsp1

####terminate
terminate

####shutdown
shutdown

exit
