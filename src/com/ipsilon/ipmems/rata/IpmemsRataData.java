/*
 * IPMEMS, the universal cross-platform data acquisition software.
 * Copyright (C) 2011, 2012 ipsilon-pro LLC.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ipsilon.ipmems.rata;

import com.ipsilon.ipmems.rata.data.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.io.IpmemsFileInfo;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.util.IpmemsCollections;
import org.ipsilon.ipmems.util.IpmemsLocalAdm;
import org.ipsilon.ipmems.util.IpmemsLocalFileNavigator;

/**
 * IPMEMS RATA data handlers.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataData extends IpmemsCollections {
	/**
	 * Executes the handler.
	 * @param ctx RATA context.
	 * @param rq Request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs exec(IpmemsRataContext ctx,
			IpmemsRataRq rq) throws Exception {
		Method m = IpmemsRataData.class.getDeclaredMethod("h",
				IpmemsRataContext.class, rq.getClass());
		return (IpmemsRataRs)m.invoke(null, ctx, rq);
	}
	
	/**
	 * Process the RATA log request.
	 * @param ctx RATA context.
	 * @param d Log request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(
			IpmemsRataContext ctx, IpmemsRataRqLog d) throws Exception {
		ctx.monitorLog(d.isEnable());
		return null;
	}
	
	/**
	 * Process the RATA command.
	 * @param ctx RATA context.
	 * @param d RATA command request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(
			IpmemsRataContext ctx, IpmemsRataRqCmd d) throws Exception {
		IpmemsInterpreter i = ctx.getInterpreter();
		long start = System.currentTimeMillis();
		try {
			Object r = i.eval("repl", d.getCommand());
			Class<?> c = r == null ? void.class : r.getClass();
			if (ctx.isStrMode()) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				PrintStream ps = i.getEngine().printStream(bos, false);
				ps.print(r);
				ps.close();
				r = bos.toString("UTF-8");
			}
			long dur = System.currentTimeMillis() - start;
			return new IpmemsRataRsResult(dur, r, c);
		} catch (Exception x) {
			long dur = System.currentTimeMillis() - start;
			return new IpmemsRataRsErrResult(dur, x);
		}
	}
	
	/**
	 * Process the file request.
	 * @param ctx RATA context.
	 * @param d RATA file request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(
			IpmemsRataContext ctx, IpmemsRataRqFile d) throws Exception {
		return new IpmemsRataRsFile(d.getFileName());
	}
	
	/**
	 * Process the RATA file tree request.
	 * @param ctx RATA context.
	 * @param d Tree request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(
			IpmemsRataContext ctx, IpmemsRataRqTree d) throws Exception {
		IpmemsFileInfo fi = d.getName().isEmpty() ?
				new IpmemsFileInfo(Ipmems.JAR_DIR) :
				new IpmemsFileInfo(new File(Ipmems.substituted(d.getName())));
		return new IpmemsRataRsTree(fi);
	}
	
	/**
	 * Process the write file request.
	 * @param ctx RATA context.
	 * @param wf Write file request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(
			IpmemsRataContext ctx, IpmemsRataRqWriteFile wf) throws Exception {
		String fn = Ipmems.substituted(wf.getName());
		File f = IpmemsLocalFileNavigator.locateScriptFile(fn);
		if (f != null) {
			IpmemsIOLib.setText(f, wf.getContents());
			return new IpmemsRataRsWriteFile(true);
		} else return new IpmemsRataRsWriteFile(false);
	}
	
	/**
	 * Process the set string mode request.
	 * @param ctx RATA context.
	 * @param sm String mode state.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(IpmemsRataContext ctx,
			IpmemsRataRqStrMode sm) throws Exception {
		ctx.setStrMode(sm.getState());
		return null;
	}
	
	/**
	 * Process the set gzipped request.
	 * @param ctx RATA context.
	 * @param gz Gzipped state.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(IpmemsRataContext ctx,
			IpmemsRataRqGzipped gz) throws Exception {
		ctx.setGzipped(gz.isGzipped());
		return null;
	}
	
	/**
	 * Process the method map request.
	 * @param ctx RATA context.
	 * @param mm Method map request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(IpmemsRataContext ctx,
			IpmemsRataRqMm mm) throws Exception {
		return new IpmemsRataRsMm(new IpmemsLocalAdm());
	}
	
	/**
	 * Process the invoke method request.
	 * @param ctx RATA context.
	 * @param im Invoke method request.
	 * @return Response.
	 * @throws Exception Any exception. 
	 */
	public static IpmemsRataRs h(IpmemsRataContext ctx,
			IpmemsRataRqInvokeMethod im) throws Exception {
		IpmemsLocalAdm adm = new IpmemsLocalAdm();
		return new IpmemsRataRsInvokeMethod(
				adm.invoke(im.getObject(), im.getMethod()));
	}
}
