package org.ipsilon.ipmems.util;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * IPMEMS gzipped serializable object.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsGzippedSerializable implements Externalizable {

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = new GZIPOutputStream(bos) {{
			def.setLevel(Deflater.BEST_COMPRESSION);
		}};
		ObjectOutputStream os = new ObjectOutputStream(gzos);
		os.writeObject(object);
		os.flush();
		gzos.finish();
		os.close();
		out.writeInt(bos.size());
		out.write(bos.toByteArray());
	}

	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Get the uncompressed object.
	 * @return Uncompressed object.
	 */
	public Object getObject() {
		return object;
	}
	
	private Object object;
}
