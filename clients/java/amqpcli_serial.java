/*===========================================================================*
 *                                                                           *
 *  amqpcli_serial.java description...                                       *
 *                                                                           *
 *  Written:        5/02/18   Your Name                                      *
 *  Revised:        5/02/18                                                  *
 *                                                                           *
 *  Skeleton generated by LIBERO 2.4 on 18 Feb, 2005, 14:39.                 *
 *===========================================================================*/
import java.awt.*;
import java.applet.*;
import java.net.*;
import java.util.*;
import java.io.*;
import com.imatix.openamq.framing.*;

public class amqpcli_serial extends amqpcli_seriali
{


///////////////////////////   P A R A M E T E R S   ///////////////////////////

// Some protocol defaults for this client
AMQConnection.Tune
    client_tune;                        /* Tune parameters                  */
AMQConnection.Open
    client_open;                        /* Connection parameters            */
AMQConnection.Close
    client_close;                       /* Default close parameters         */
String
    opt_server = "localhost";           /* Remote server                    */
short
    protocol_port = 7654,               /* Server port                      */
    protocol_id = 128,                  /* Protocol id                      */
    protocol_ver = 1,                   /* Protocol port                    */
    batch_size = 1000,                  /* Messages prefetched before ACK   */
    client_nbr = 0;                      /* Added to the name prefix         */
int
    socket_timeout = 0;                 /* Socket timeout im ms             */


//////////////////////////////   G L O B A L S   //////////////////////////////

// Network streams
OutputStream
    amqp_out = null;                    /* Outgoing connection              */
InputStream
    amqp_in = null;                     /* Incoming connectionn             */
// Messages
String
    error_message,                      /* Error details                    */
    module;                             /* Module in which error occured    */
Exception
    exception = null;                   /*                                  */
// Framing utility
AMQFramingFactory
    amq_framing = null;                 /* Framing utility                  */


///////////////////////////   C O N T R U C T O R S  //////////////////////////

public amqpcli_serial () {
}

public amqpcli_serial (String args[])
{
    amqpcli_serial_execute(args);
}


//////////////////////////////////   M A I N   ////////////////////////////////

public static void main (String args[])
{
    amqpcli_serial                      /* The client object                */
        single = new amqpcli_serial(args);

}

public int amqpcli_serial_execute (String args[])
{
    int
        feedback;                       /* Console return int               */
    
    if (args.length > 0)
        client_nbr = Short.parseShort(args[0]);       

    feedback = execute ();

    return (feedback);
}


//////////////////////////   INITIALISE THE PROGRAM   /////////////////////////

public void initialise_the_program ()
{
    the_next_event = ok_event;
}


////////////////////////////   GET EXTERNAL EVENT   ///////////////////////////

public void get_external_event ()
{
}

//%START MODULE

//////////////////////////////////   SETUP   //////////////////////////////////

public void setup ()
{
    try
    {
        Socket
            amqp = null;                /* Network socket                   */

        // Network setup
        amqp = new Socket(opt_server, protocol_port);
        if (socket_timeout > 0)
            amqp.setSoTimeout(socket_timeout);
        amqp_in = amqp.getInputStream();
        amqp_out = amqp.getOutputStream();
        amq_framing = new AMQFramingFactory(amqp);

        // Client tune capabilities
        client_tune = (AMQConnection.Tune)amq_framing.createFrame(AMQConnection.TUNE);
        client_tune.frameMax = 2000;
        client_tune.channelMax = 128;
        client_tune.handleMax = 128;
        client_tune.heartbeat = 40;
        client_tune.options = null;

        // Client name and connection open defaults
        client_open = (AMQConnection.Open)amq_framing.createFrame(AMQConnection.OPEN);
        client_open.confirmTag = 0;
        client_open.virtualPath = null;
        client_open.clientName = "java/amqpcli_serial (test)" + (client_nbr > 0 ? String.valueOf(client_nbr) : "");
        client_open.options = null;

        // Connection close defaults
        client_close = (AMQConnection.Close)amq_framing.createFrame(AMQConnection.CLOSE);
        client_close.replyCode = 200;
        client_close.replyText = "amqpcli_serial.java: bye";
    }
    catch (UnknownHostException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "setup", "unknown host");
    } 
    catch (SocketTimeoutException e) {
        raise_exception(timeout_event, e, "amqpci_java", "setup", "SocketTimeoutException");
    }
    catch (IOException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "setup", "IOException");
    }
    catch (AMQFramingException e) {}

    the_next_event = send_connection_initiation_event;
}


//////////////////////////////   HANDLE TIMEOUT   /////////////////////////////

public void handle_timeout ()
{
    forced_shutdown ();
    System.out.println("java/amqpcli_serial currently does not handle network timeout, terminating.");
    System.exit(1);
}


/////////////////////////////   FORCED SHUTDOWN   /////////////////////////////

public void forced_shutdown ()
{
    // Close connection
    try
    {
        if (amqp_in != null)
            amqp_in.close();
        if (amqp_out != null)
            amqp_out.close();

        if (exception != null)
            AMQFramingFactory.error(exception, "java/amqpcli_serial", module, error_message);
    }
    catch (IOException e) {}
    catch (AMQFramingException e) {}
}


//////////////////////////   TERMINATE THE PROGRAM   //////////////////////////

public void terminate_the_program ()
{
    System.out.println("java/amqpcli_serial terminating.");
    System.exit(exception != null ? 1 : 0);
}


////////////////////////   SEND CONNECTION INITIATION   ///////////////////////

public void send_connection_initiation ()
{
    try
    {
        // Send initiation
        amqp_out.write(protocol_id);
        amqp_out.write(protocol_ver);
    }
    catch (IOException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "send_connection_initiation", "unable to connect");
    }

    the_next_event = connection_challenge_event;
}


///////////////////////////   SEND CONNECTION OPEN   //////////////////////////

public void send_connection_open ()
{
    try
    {
        client_open.virtualPath = "/test";
        amq_framing.produceFrame(client_open);
    }
    catch (AMQFramingException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "send_connection_open", "cannot open connection");
    }

    the_next_event = do_tests_event;
}


/////////////////////////////////   DO TESTS   ////////////////////////////////

public void do_tests ()
{
    try
    {
        // Channel
        AMQChannel.Open                 /* Channel open command             */
            channel_open = (AMQChannel.Open)amq_framing.createFrame(AMQChannel.OPEN);
        AMQChannel.Commit               /* Channel commit command           */
            channel_commit = (AMQChannel.Commit)amq_framing.createFrame(AMQChannel.COMMIT);
        AMQChannel.Ack                  /* Channel ack command              */
            channel_ack = (AMQChannel.Ack)amq_framing.createFrame(AMQChannel.ACK);
        AMQChannel.Close                /* Channel close command            */
            channel_close = (AMQChannel.Close)amq_framing.createFrame(AMQChannel.CLOSE);
        // Handle
        AMQHandle.Open                  /* Handle open command              */
            handle_open = (AMQHandle.Open)amq_framing.createFrame(AMQHandle.OPEN);
        AMQHandle.Send                  /* Handle send command              */
            handle_send = (AMQHandle.Send)amq_framing.createFrame(AMQHandle.SEND);
        AMQHandle.Consume               /* Handle consume command           */
            handle_consume = (AMQHandle.Consume)amq_framing.createFrame(AMQHandle.CONSUME);
        AMQHandle.Flow                  /* Handle flow command              */
            handle_flow = (AMQHandle.Flow)amq_framing.createFrame(AMQHandle.FLOW);
        AMQHandle.Notify
            handle_notify = null;       /* Handle notify reply              */
        AMQHandle.Created
            handle_created;             /* Handle created reply             */
        // Message
        AMQMessage.Head                 /* Message header                   */
            message_head = (AMQMessage.Head)amq_framing.createMessageHead();
        byte[]
            message_body;               /* Message body                     */

        // Open channel
        channel_open.channelId = 1;
        channel_open.confirmTag = 0;
        channel_open.transacted = true;
        channel_open.restartable = false;
        channel_open.options = null;
        channel_open.outOfBand = "";
        amq_framing.produceFrame(channel_open);

        // Open hadle
        handle_open.channelId = 1;
        handle_open.handleId = 1;
        handle_open.serviceType = 1;
        handle_open.confirmTag = 0;
        handle_open.producer = true;
        handle_open.consumer = true;
        handle_open.browser = false;
        handle_open.temporary = true;
        handle_open.destName = "";
        handle_open.mimeType = "";
        handle_open.encoding = "";
        handle_open.options = null;
        amq_framing.produceFrame(handle_open);
        // Get handle created
        handle_created = (AMQHandle.Created)amq_framing.consumeFrame();

        // Pause incoming messages
        handle_flow.handleId = 1;
        handle_flow.confirmTag = 0;
        handle_flow.flowPause = true;
        amq_framing.produceFrame(handle_flow);

        // Prepare commit and ack
        channel_commit.channelId = 1;
        channel_commit.confirmTag = 0;
        channel_commit.options = null;
        channel_ack.channelId = 1;
        channel_ack.confirmTag = 0;
        channel_ack.messageNbr = 0;

        // Send handles and growing messages; commit on the fly
        handle_send.handleId = 1;
        handle_send.confirmTag = 0;
        handle_send.fragmentSize = 0;
        handle_send.partial = false;
        handle_send.outOfBand = false;
        handle_send.recovery = false;
        handle_send.streaming = false;
        handle_send.destName = "";
        message_head.bodySize = 0;
        message_head.persistent = false;
        message_head.priority = 1;
        message_head.expiration = 0;
        message_head.mimeType = "";
        message_head.encoding = "";
        message_head.identifier = "";
        message_head.headers = null;
        System.out.println("Sending " + (client_tune.frameMax << 1) + " fragmented messages of increasing size to server...");
        int i = 1;
        for (; true; i++) {
            // Create the message body
            message_head.bodySize = i;
            message_body = new byte[i];
            body_fill(message_body, i);
            // Set the fragment size
            handle_send.fragmentSize = (byte)(Math.random() * Math.min(i, 64)); // Data chunk to send along the header
            handle_send.partial = handle_send.fragmentSize < message_head.bodySize; // Will there be more to send?
            handle_send.fragmentSize += message_head.encode(); // Complete the fragment size
            if (message_head.bodySize > (client_tune.frameMax << 1))
                break;
            // Send message
            amq_framing.produceFrame(handle_send);
            amq_framing.produceMessageHead(message_head);
            amq_framing.produceInBandMessageBody(handle_send, message_head, message_body);
            // Commit from time to time
            if (i % batch_size == 0) {
                amq_framing.produceFrame(channel_commit);
                System.out.println("Commit batch " + (i / batch_size) + "...");
            }
        }
        // Commit leftovers
        amq_framing.produceFrame(channel_commit);
        System.out.println("Commit final batch...");

        // Resume incoming messages
        handle_flow.flowPause = false;
        amq_framing.produceFrame(handle_flow);

        // Read back
        handle_consume.handleId = 1;
        handle_consume.confirmTag = 0;
        handle_consume.prefetch = batch_size;
        handle_consume.noLocal = false;
        handle_consume.unreliable = false;
        handle_consume.destName = "";
        handle_consume.identifier = "";
        handle_consume.selector = null;
        handle_consume.mimeType = "";
        // Request consume messages
        amq_framing.produceFrame(handle_consume);
        System.out.println("Reading messages back from the server...");
        int j = 1;
        for (i--; i > 0; i--, j++) {
            byte[] bytes;
            // Get handle notify
            handle_notify = (AMQHandle.Notify)amq_framing.consumeFrame();
            message_head = amq_framing.consumeMessageHead();
            bytes = amq_framing.consumeInBandMessageBody(handle_notify, message_head);
            if (bytes.length != j) {
                System.err.println("amqpcli_serial: body_check: returning message size mismatch (is " + bytes.length + " should be " + j + ").");
                System.exit(1);
            }
            body_check(bytes, j);
            // Acknowledge & commit from time to time
            if (j % batch_size == 0) {
                channel_ack.messageNbr = handle_notify.messageNbr;
                amq_framing.produceFrame(channel_ack);
                amq_framing.produceFrame(channel_commit);
                System.out.println("Acknowledge batch " + (j / batch_size) + "...");
            }
        }
        // Acknowledge & commit leftovers
        channel_ack.messageNbr = handle_notify.messageNbr;
        amq_framing.produceFrame(channel_ack);
        amq_framing.produceFrame(channel_commit);
        System.out.println("Acknowledge final batch...");

        // Say bye
        channel_close.channelId = 1;
        channel_close.replyCode = 200;
        channel_close.replyText = "amqpcli_serial.java: I'll be back";
        amq_framing.produceFrame(channel_close);
        channel_close = (AMQChannel.Close)amq_framing.consumeFrame();
        amq_framing.produceFrame(client_close);
        client_close = (AMQConnection.Close)amq_framing.consumeFrame();
        System.out.println("Closing, server says: " + client_close.replyText + ".");
    }
    catch (ClassCastException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "do_tests", "unexpected frame from server");
    }
    catch (IOException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "do_tests", "IOException");
    }
    catch (AMQFramingException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "do_tests", "framing error");
    }

    the_next_event = done_event;
}



/////////////////////////   GET CONNECTION CHALLENGE   ////////////////////////

public void face_connection_challenge ()
{
    try
    {
        AMQConnection.Challenge         /* Challenge from server            */
            challenge = (AMQConnection.Challenge)amq_framing.consumeFrame();
        AMQConnection.Response          /* our response                     */
            response = (AMQConnection.Response)amq_framing.createFrame(AMQConnection.RESPONSE);
        // Send the response
        response.mechanism = "plain";
        response.responses = null;
        amq_framing.produceFrame(response);
    }
    catch (ClassCastException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "face_connection_challenge", "unexpected frame from server");
    }
    catch (SocketTimeoutException e) {
        raise_exception(timeout_event, e, "amqpci_java", "face_connection_challenge", "SocketTimeoutException");
    }
    catch (IOException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "face_connection_challenge", "IOException");
    }
    catch (AMQFramingException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "face_connection_challenge", "authentication error");
    }

    the_next_event = connection_tune_event;
}


////////////////////////   NEGOTIATE CONNECTION TUNE   ////////////////////////

public void negotiate_connection_tune ()
{
    try
    {
        AMQConnection.Tune              /* Tune parameters from server      */
            tune_server = (AMQConnection.Tune)amq_framing.consumeFrame();

        // Send the reply
        client_tune.frameMax = (short)Math.min(client_tune.frameMax, tune_server.frameMax);
        client_tune.channelMax = (short)Math.min(client_tune.channelMax, tune_server.channelMax);
        client_tune.handleMax = (short)Math.min(client_tune.handleMax, tune_server.handleMax);
        client_tune.heartbeat = (short)Math.min(client_tune.heartbeat, tune_server.heartbeat);
        amq_framing.produceFrame(client_tune);
    }
    catch (ClassCastException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "negotiate_connection_tune", "unexpected frame from server");
    }
    catch (SocketTimeoutException e) {
        raise_exception(timeout_event, e, "amqpci_java", "negotiate_connection_tune", "SocketTimeoutException");
    }
    catch (IOException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "negotiate_connection_tune", "IOException");
    }
    catch (AMQFramingException e)
    {
        raise_exception(exception_event, e, "amqpci_java", "negotiate_connection_tune", "tune error");
    }
}


/////////////////////////////////   SHUTDOWN   ////////////////////////////////

public void shutdown ()
{
}


//- Standard dialog routines --------------------------------------------
public void raise_exception (int event, Exception e, String _class, String module, String message)
{
    this.error_message = message;
    this.module = module;
    this.exception = e;

    System.err.println(e.getMessage());
    System.err.println(_class + ": " + module + ": " + message + ".");

    // Reset message
    error_message = null;
    module = null;
    exception = null;

    raise_exception (event);
}


//- Auxiliary routines --------------------------------------------
byte[] body_fill(byte[] body, int seed) {
    int a = 1664525, b = 1013904223;
    long m = (long)1 << 32, v = seed;

    // Fill with patterns from a linear congruential generator
    for (int i = 0; i < body.length; i++) {
        v = (a * v + b) & (m - 1);
        body[i] = (byte)Math.max((byte)(v % Byte.MAX_VALUE), 1); // No zeros
    }

    return body;
}

void body_check(byte[] body, int seed) {
    byte[] ref = new byte[body.length];

    ref = body_fill(ref, seed);
    for (int i = 0; i < body.length; i++) {
        if (body[i] != ref[i]) {
            System.out.println("Received:");
            dump_array(body);
            System.out.println("Reference:");
            dump_array(ref);
            System.err.println("amqpcli_serial: body_check: returning message contents mismatch.");
            System.exit(1);
        }
    }
}

void dump_array(byte[] body) {
    for (int i = 0; i < body.length; i++) {
        if (body[i] < 10)
            System.out.print("0");
        if (body[i] < 100)
            System.out.print("0");
        System.out.print(body[i]);
        System.out.print(" ");
    }
    System.out.println("");
}


//%END MODULE
}
