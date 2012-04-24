#!/usr/bin/python

import re

nonterm = set()
for s in open('Vocab.gr', 'r'):
    idx = s.find("#")
    if idx != -1 :
        s = s[0:idx]
    if s.strip() != "":
        nonterm.add(s.split()[1])

nonterm = sorted(nonterm)

def write_all(prefix,sep):      
    print "1\t"+prefix

    for nt in nonterm:
        print "1\t"+prefix+sep+"_"+nt

write_all("S2","\t")
for nt in nonterm:
    write_all("_"+nt+"\t"+nt," ")

