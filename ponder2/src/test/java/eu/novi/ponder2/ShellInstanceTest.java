package eu.novi.ponder2;

import static org.hamcrest.Matchers.instanceOf;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;

import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

public class ShellInstanceTest extends TestCase {

	public ShellInstanceTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testShellInstance() {
		/*
		 * Removed by Yiannos Mockery m = new Mockery() {{
		 * setImposteriser(ClassImposteriser.INSTANCE); }}; final Socket sc =
		 * m.mock(Socket.class,"sc"); final DomainP2Adaptor dap =
		 * m.mock(DomainP2Adaptor.class); final Socket socket =
		 * m.mock(Socket.class,"socket"); final OutputStream out =
		 * m.mock(OutputStream.class); final InputStream in =
		 * m.mock(InputStream.class); ShellInstance si = null; final
		 * ShellInstanceP2Adaptor shellAdaptor = new ShellInstanceP2Adaptor();
		 * shellAdaptor.setObj(si); final Sequence seq = m.sequence("seq"); try
		 * { m.checking(new Expectations() {{ one (sc);
		 * will(returnValue(socket)); inSequence(seq); one
		 * (socket).getOutputStream(); will(returnValue(out)); inSequence(seq);
		 * one (socket).getInputStream(); will(returnValue(in));
		 * inSequence(seq); one
		 * (dap).operation(with(equal(SelfManagedCell.RootDomain)),
		 * with(equal("at:put:")),
		 * (P2Object)with(instanceOf(P2Object[].class))); inSequence(seq); one
		 * (out).flush(); inSequence(seq); one (in).close(); inSequence(seq);
		 * }}); si = new ShellInstance(sc, P2Object.create(7), dap, 12);
		 * assertNotNull(si);
		 * 
		 * Field f = ShellInstance.class.getDeclaredField("root");
		 * f.setAccessible(true);
		 * assertEquals(((P2Object)f.get(si)).asInteger(), 7);
		 * 
		 * f = ShellInstance.class.getDeclaredField("socket");
		 * f.setAccessible(true); assertEquals(f.get(si), socket);
		 * 
		 * f = ShellInstance.class.getDeclaredField("out");
		 * f.setAccessible(true); assertTrue(f.get(si) instanceof PrintWriter);
		 * ((PrintWriter)f.get(si)).flush();
		 * 
		 * f = ShellInstance.class.getDeclaredField("in");
		 * f.setAccessible(true); assertTrue(f.get(si) instanceof
		 * BufferedReader); ((BufferedReader)f.get(si)).close();
		 * 
		 * f = ShellInstance.class.getDeclaredField("shellmo");
		 * f.setAccessible(true);
		 * assertEquals(((ShellInstanceP2Adaptor)f.get(si)).getObj(), si);
		 * 
		 * f = ShellInstance.class.getDeclaredField("currentDomain");
		 * f.setAccessible(true);
		 * assertEquals(((P2Object)f.get(si)).asInteger(), 7);
		 * 
		 * f = ShellInstance.class.getDeclaredField("currentPath");
		 * f.setAccessible(true); assertEquals(((Path)f.get(si)).toString(),
		 * "/");
		 * 
		 * f = ShellInstance.class.getDeclaredField("variables");
		 * f.setAccessible(true); assertNotNull(f.get(si));
		 * assertEquals(((P2Hash)f.get(si)).get("Variables"), f.get(si));
		 * assertEquals(((P2Hash)f.get(si)).size(), 1);
		 * 
		 * m.assertIsSatisfied(); } catch (Exception e) {fail(e.toString());}
		 */
	}

	public void testRun() {
		/*
		 * Removed by Yiannos
		 * System.out.println("ShellInstanceTest.testRun():"); Mockery m = new
		 * Mockery() {{ setImposteriser(ClassImposteriser.INSTANCE); }}; final
		 * Socket sc = m.mock(Socket.class,"sc"); final Socket socket =
		 * m.mock(Socket.class,"socket"); final PrintWriter out =
		 * m.mock(PrintWriter.class); final BufferedReader in =
		 * m.mock(BufferedReader.class); final String version = "Ponder2 Shell "
		 * + SelfManagedCell.SVNRevision.substring(1,
		 * SelfManagedCell.SVNRevision.length() - 2) + " " +
		 * SelfManagedCell.SVNDate.substring(1, SelfManagedCell.SVNDate.length()
		 * - 2) + "\r\nNo wild chars yet\r\n"; final Sequence seq =
		 * m.sequence("seq"); try { m.checking(new Expectations() {{ one (sc);
		 * will(returnValue(socket)); inSequence(seq); one
		 * (socket).getOutputStream(); will(returnValue(System.out));
		 * inSequence(seq); one (socket).getInputStream();
		 * will(returnValue(System.in)); inSequence(seq); one
		 * (out).print(with(equal(version))); inSequence(seq); one
		 * (out).print("/ $ "); inSequence(seq); one (out).flush();
		 * inSequence(seq); one (in).readLine(); will(returnValue("ls"));
		 * inSequence(seq); one (out).print("/\r\n"); inSequence(seq); one
		 * (out).flush(); inSequence(seq); one (out).print("/ $ ");
		 * inSequence(seq); one (out).flush(); inSequence(seq); one
		 * (in).readLine(); will(returnValue(null)); inSequence(seq); one
		 * (in).close(); inSequence(seq); one (out).close(); inSequence(seq);
		 * }}); DomainP2Adaptor dom = new DomainP2Adaptor(null, "create",
		 * P2Object.create()); ShellInstance si = new ShellInstance(sc,
		 * P2Object.create(7), dom, 12); Field f =
		 * ShellInstance.class.getDeclaredField("in"); f.setAccessible(true);
		 * f.set(si, in); f = ShellInstance.class.getDeclaredField("out");
		 * f.setAccessible(true); f.set(si, out); si.run();
		 * m.assertIsSatisfied(); } catch (Exception e) {fail(e.toString());}
		 * System.out.println();
		 */
	}

	public void testDummyop() {
		// this is effectively the same code as for the constructor,
		// but it does some calls of dummyop() and checks that it does no
		// changes
		// to the values of the instantiated ShellInstance-class.
		/*
		 * Removed By Yiannos Mockery m = new Mockery() {{
		 * setImposteriser(ClassImposteriser.INSTANCE); }}; final Socket sc =
		 * m.mock(Socket.class,"sc"); final DomainP2Adaptor dap =
		 * m.mock(DomainP2Adaptor.class); final Socket socket =
		 * m.mock(Socket.class,"socket"); final OutputStream out =
		 * m.mock(OutputStream.class); final InputStream in =
		 * m.mock(InputStream.class); ShellInstance si = null; final
		 * ShellInstanceP2Adaptor shellAdaptor = new ShellInstanceP2Adaptor();
		 * shellAdaptor.setObj(si); final Sequence seq = m.sequence("seq"); try
		 * { m.checking(new Expectations() {{ one (sc);
		 * will(returnValue(socket)); inSequence(seq); one
		 * (socket).getOutputStream(); will(returnValue(out)); inSequence(seq);
		 * one (socket).getInputStream(); will(returnValue(in));
		 * inSequence(seq); one
		 * (dap).operation(with(equal(SelfManagedCell.RootDomain)),
		 * with(equal("at:put:")),
		 * (P2Object)with(instanceOf(P2Object[].class))); inSequence(seq); one
		 * (out).flush(); inSequence(seq); one (in).close(); inSequence(seq);
		 * }}); si = new ShellInstance(sc, P2Object.create(7), dap, 12);
		 * si.dummyop(); assertNotNull(si); si.dummyop();
		 * 
		 * Field f = ShellInstance.class.getDeclaredField("root");
		 * f.setAccessible(true);
		 * assertEquals(((P2Object)f.get(si)).asInteger(), 7);
		 * 
		 * f = ShellInstance.class.getDeclaredField("socket");
		 * f.setAccessible(true); assertEquals(f.get(si), socket);
		 * 
		 * f = ShellInstance.class.getDeclaredField("out");
		 * f.setAccessible(true); assertTrue(f.get(si) instanceof PrintWriter);
		 * ((PrintWriter)f.get(si)).flush();
		 * 
		 * f = ShellInstance.class.getDeclaredField("in");
		 * f.setAccessible(true); assertTrue(f.get(si) instanceof
		 * BufferedReader); ((BufferedReader)f.get(si)).close();
		 * 
		 * f = ShellInstance.class.getDeclaredField("shellmo");
		 * f.setAccessible(true);
		 * assertEquals(((ShellInstanceP2Adaptor)f.get(si)).getObj(), si);
		 * 
		 * f = ShellInstance.class.getDeclaredField("currentDomain");
		 * f.setAccessible(true);
		 * assertEquals(((P2Object)f.get(si)).asInteger(), 7);
		 * 
		 * f = ShellInstance.class.getDeclaredField("currentPath");
		 * f.setAccessible(true); assertEquals(((Path)f.get(si)).toString(),
		 * "/");
		 * 
		 * f = ShellInstance.class.getDeclaredField("variables");
		 * f.setAccessible(true); assertNotNull(f.get(si));
		 * assertEquals(((P2Hash)f.get(si)).get("Variables"), f.get(si));
		 * assertEquals(((P2Hash)f.get(si)).size(), 1);
		 * 
		 * m.assertIsSatisfied(); } catch (Exception e) {fail(e.toString());}
		 */
	}

}
