#!/usr/bin/perl
# StreamCancelOk.pm

# Generated from amq.asl by asl_chassis_perl.gsl using GSL/4.
# DO NOT MODIFY THIS FILE.

package AMQP::Framing::Frames::StreamCancelOk;

use strict;
use warnings;

# Constructor
sub new($$) {
    my $class = ref($_[0]) || $_[0];
    my $self = bless {}, $class;

    $self->{'codec'} = $_[1];

    $self;
}

# Accessors
sub amqp_class_id() {
    80;
}

sub amqp_method_id() {
    31;
}

sub consumer_tag($;$) {
    my ($self, $consumer_tag) = @_;
    my $old_consumer_tag = undef;

    if ($consumer_tag) {
        $old_consumer_tag = $self->{'consumer_tag'};
        $self->{'consumer_tag'} = $consumer_tag;

        $old_consumer_tag;
    } else {
        $self->{'consumer_tag'};
    }
}


sub body_size($) {
    $self = shift;

    0 +
        $self->{'codec'}->size_shortstr($self->{'consumer_tag'});
}

# Marshalling
sub write_method($) {
    my $self = shift;

    $self->{'codec'}->write_shortstr($self->{'consumer_tag'});
}

sub read_method($) {
    my $self = shift;

    $self->{'consumer_tag'} = $self->{'codec'}->read_shortstr();
}

sub print($$) {
    my $self = shift;
    my $HANDLE = shift;

    $HANDLE->printf("\n\tconsumer_tag: %s", "$self->{'consumer_tag'}");
}

1;
