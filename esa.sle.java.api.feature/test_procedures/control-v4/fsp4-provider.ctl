# fsp4-provider.ctl
start

###CREATE-FSP-SI
create_si
FSP             #service_type
fsp=fsp4        #name
p               #provider
4               #version
down
fsp=fsp4
set_peer_id  	#set_peer_identifier
tester1
set_pp          #set_provision_period
void
void
set_bind_ini  	#set_bind_initiative
u
set_rsp_port_id #set_responder_port
Harness_Port_1
set_rtn_to      #set_return_timeout
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
set_ps
1       #operationalBD
0       #fop alert(no)
1024    #bufferSize
y       #notify
NULL
set_ps
2       #operationalADandBD
0       #fop alert(no)
1024    #bufferSize
y       #notify
NULL
print
config_completed

wait_event      #incoming_bind_operation
60
1
y               #send_positive_return

wait_event      #incoming_start_operation
10
1
y               #send_positive_return

spd             #setup_start_production_time
2018-09-05T00:00:00

id              #first_packetid_the_provider_shall_accept
0
ok              #end_config
 
wait_event      #incoming_directive_operation
10
1
r
eid
2
diag
1
ok 

wait_event      #incoming_directive_operation
10
1
r
eid
3
ok 

wait_event      #incoming_throw_event_operation
60
1
r               #setup_and_send_return
eeid            #expected_id
12345
ok 

wait_event      #incoming_stop_operation
30
1
y               #send_positive_return

wait_event      #incoming_unbind_operation
30
1
y               #send_positive_return

###DESTROY-SI
wait            #plain_wait_prior_to_destroy_si
1
up
destroy_si
fsp=fsp4

terminate
shutdown
exit
