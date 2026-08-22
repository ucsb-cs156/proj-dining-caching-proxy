package edu.ucsb.cs156.diningcachingproxy.services;

/** A host (hostname if resolved, else IP address) and how many requests it has sent. */
public record HostCount(String host, long count) {}
