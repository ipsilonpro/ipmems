public class IpmemsRxTxIO extends IpmemsObservablePropertized implements IpmemsIO {
	public IpmemsRxTxIO() {
		this(Collections.EMPTY_MAP);
	}
	
	public IpmemsRxTxIO(Map props) {
		super(props);
	}
	
	@Override
	public void connect() {
		def port = get(String, "port", "/dev/ttyS0");
		def owner = get(String, "owner", "ipmems");
		def pdelay = get(Integer, "pdelay", 2000);
		rxtxPort = cp.getPortIdentifier(port).open(owner, pdelay);
		if (sp.isInstance(rxtxPort)) {
			rxtxPort.setSerialPortParams(
				get(Integer, "baud", 9600),
				get(Integer, "databits", sp.DATABITS_8),
				get(Integer, "stopbits", sp.STOPBITS_1),
				get(Integer, "parity", sp.PARITY_NONE));
			rxtxPort.setFlowControlMode(get(Integer, "fc", sp.FLOWCONTROL_NONE));
		}
		active = true;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return rxtxPort.getInputStream();
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		return rxtxPort.getOutputStream();
	}
	
	@Override
	public void close() throws IOException {
		active = false;
		IOException z = null;
		try {
			rxtxPort.getOutputStream().close();
		} catch (IOException x) {
			if (z == null) z = x;
		}
		try {
			rxtxPort.getInputStream().close();
		} catch (IOException x) {
			if (z == null) z = x;
		}
		try {
			rxtxPort.close();
		} catch (IOException x) {
			if (z == null) z = x;
		}
		if (z != null) throw z;
	}
	
	@Override
	public boolean isActive() throws IOException {
		return active;
	}
	
	@Override
	public Object getTransceiver() {
		return rxtxPort;
	}
	
	@Override
	public String toString() {
		return rxtxPort != null ? rxtxPort.toString() : "null";
	}
	
	private volatile Object rxtxPort;
	private volatile boolean active;
	
	private static ClassLoader loader = new URLClassLoader(
		[new File(Ipmems.get("rxtxDir", "/usr/share/java"),
					"RXTXcomm.jar").toURI().toURL()] as URL[]);
	private static Class<?> cp = loader.loadClass("gnu.io.CommPortIdentifier");
	private static Class<?> sp = loader.loadClass("gnu.io.SerialPort");
}