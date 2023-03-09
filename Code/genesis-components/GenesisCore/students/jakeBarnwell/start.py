#!/usr/bin/env python 
 
import pprint 
import requests 
 
START_API='http://start.csail.mit.edu/api.php?' 
 
payload = {'query':'', 
	'referrer':'http://start.csail.mit.edu/', 
	'server':'guest', 
	'machine':'kiribati', 
	'action':'parse', 
	'te':'formatted-text'} 
 
def parse(query): 
	payload['query'] = query 
	r = requests.post(START_API, params=payload) 
	#pprint.pprint(filter(lambda x: x and '===>' not in x, r.text.strip().split('\n'))) 
	# return map(texpstr_to_texp, filter(lambda x: x and '===>' not in x, r.text.strip().split('\n'))) 
	return r.text.strip().split('\n')
 
def texpstr_to_texp(texpstr): 
	s, r, o = texpstr[1:-1].split() 
	return {'subject':s, 'relation':r, 'object':o} 
 
if __name__=='__main__': 
	query = raw_input().strip() 
	while query != 'QUIT': 
		texps = parse(query) 
		pprint.pprint(texps) 
		query = raw_input().strip()