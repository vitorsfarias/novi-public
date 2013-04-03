///**
// * Copyright 2005 Imperial College, London, England.
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or (at your option) any later version.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General
// * Public License along with this library; if not, write to the
// * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
// * Boston, MA 02110-1301 USA
// *
// * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
// *
// * Created on Mar 24, 2006
// *
// * $Log:$
// */
//
//package eu.novi.ponder2.comms.http;
//
//import java.net.URI;
//import java.rmi.RemoteException;
//
//import javax.xml.namespace.QName;
//import javax.xml.rpc.Call;
//
//import eu.novi.ponder2.OID;
//import eu.novi.ponder2.exception.Ponder2Exception;
//import eu.novi.ponder2.exception.Ponder2RemoteException;
//import eu.novi.ponder2.objects.P2Object;
//
//import org.apache.axis.client.Service;
//
//import eu.novi.ponder2.comms.Transmitter;
//import eu.novi.ponder2.comms.TransmitterImpl;
//
//
//
///**
// * Class for sending commands to remote managed objects using Web Services via
// * HTTP
// *
// * @author Kevin Twidle
// * @version $Id:$
// */
//public class HTTPTransmitter extends TransmitterImpl implements Transmitter {
//
//  @Override
//  public Transmitter connect(URI location) {
//    // We use the location in every call, we can just return ourself
//    return this;
//  }
//
////  /*
////   * (non-Javadoc)
////   *
////   * @see net.ponder2.comms.Transmitter#getObject(java.net.URI,
////   *      java.lang.String, net.ponder2.Result)
////   */
////  public P2OID getObject(URI location, String path) {
////    return call(location, "getObject", location.toString(), path);
////  }
//
//  /* (non-Javadoc)
//   * @see net.ponder2.comms.Transmitter#getObject(java.net.URI, java.lang.String)
//   */
//  @Override
//  public P2Object getObject(URI address, String path) throws Ponder2Exception {
//    return getObjectString(address, path);
//  }
//
//  /* (non-Javadoc)
//   * @see net.ponder2.comms.Transmitter#execute(java.net.URI, net.ponder2.OID, net.ponder2.objects.P2Object, java.lang.String, net.ponder2.objects.P2Object[])
//   */
//  @Override
//  public P2Object execute(URI address, OID target, P2Object source, String op, P2Object[] args) throws Ponder2Exception {
//    return executeString(address, target, source, op, args);
//  }
//
//
//  /* (non-Javadoc)
//   * @see net.ponder2.comms.TransmitterImpl#execute(java.net.URI, java.lang.String)
//   */
//  @Override
//  protected String execute(URI address, String xmlString) throws Ponder2Exception {
//    try {
//    Service service = new Service();
//    Call call = service.createCall();
//
//    call.setOperationName(new QName(address.toString(), "execute"));
//    call.setTargetEndpointAddress(address.toString());
//       return (String)call.invoke(new Object[] {xmlString});
//    }
//    catch (Exception e) {
//      throw new Ponder2RemoteException(e.getMessage());
//    }
//  }
//
//  /**
//   * sets up and performs the actual call to the remote Web Service
//   *
//   * @param endpoint
//   *          the address of the remote web service
//   * @param operation
//   *          the operaton to be performed at the remote site
//   * @param args
//   *          an array of string arguments for the operation
//   * @return the standard result structure from the remote site containing OIDs
//   *         or errors
//   * @throws RemoteException
//   * @throws Exception
//   */
//  public static P2Object call(URI endpoint, String operation, String... args) throws Exception {
//      Service service = new Service();
//      Call call = service.createCall();
//
//      call.setOperationName(new QName(endpoint.toString(), operation));
//      call.setTargetEndpointAddress(endpoint.toString());
//      call.invoke(args);
//      System.out.println("HTTPTransmitter: Invoke returns ");
//      return null;
//  }
//
//
//
//}
