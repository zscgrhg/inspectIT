package rocks.inspectit.server.spring.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import rocks.inspectit.shared.all.serializer.ISerializer;
import rocks.inspectit.shared.all.serializer.SerializationException;
import rocks.inspectit.shared.all.serializer.provider.SerializationManagerProvider;

/**
 * This service exporter using kryo for (de-)serialization is nearly the same as the one with plain
 * java serialization.
 *
 * @author Patrice Bouillet
 *
 */
public class KryoHttpInvokerServiceExporter extends HttpInvokerServiceExporter {

	/**
	 * The serialization manager.
	 */
	@Autowired
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is) throws IOException, ClassNotFoundException {
		try (Input input = new Input(is)) {
			ISerializer serializer = serializationManagerProvider.createSerializer();
			return (RemoteInvocation) serializer.deserialize(input);
		} catch (SerializationException e) {
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeRemoteInvocationResult(HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result, OutputStream os) throws IOException {
		try (Output output = new Output(os)) {
			if (!result.hasException()) {
				Object value = result.getValue();
				result = new RemoteInvocationResult(value);
			}

			ISerializer serializer = serializationManagerProvider.createSerializer();
			serializer.serialize(result, output);
		} catch (SerializationException e) {
			throw new IOException(e);
		}
	}

}
