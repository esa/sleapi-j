# fsp1-provider.ctl
start

###CREATE-FSP-SI
create_si
FSP             #service_type
fsp=fsp1        #name
p               #provider
4               #version
down
fsp=fsp1
set_peer_id
tester1
set_pp          #set_provision_period
void
void
set_bind_ini    #set_bind_initiative
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
1000000
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

epd             #setup_end_production_time
2019-09-05T00:00:00
ok              #end_config

###wait_transfer_data_invoke
wait_event
30
1
r    #setup_and_send_return
eid  #expected_directive_id
1235
pba
2134
sp
ok

wait
2

pkt_started
1234
1
2017-08-01T10:00:00
1244
y

wait
1

pkt_rad
1234
1
2017-08-01T10:10:00
y

###wait_transfer_data_invoke
wait_event
30
1
r    #setup_and_send_return
eid  #expected_directive_id
1236
pba
2355
sp
ok

wait
2

pkt_started
1235
0
2017-08-01T10:20:00
1214
y

wait
1

pkt_rad
1235
0
2017-08-01T10:30:00
y

wait
1

pkt_ack
1235
2017-08-01T10:40:00
y

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
fsp=fsp1

terminate
shutdown
exit
