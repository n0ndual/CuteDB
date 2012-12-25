package org.tigeress;

import org.tigeress.connector.Request;
import org.tigeress.connector.RequestHandler;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.dsl.Disruptor;

public class DispatchRequestHandler implements RequestHandler {

	private Disruptor<Request> disruptor;

	public DispatchRequestHandler(Disruptor<Request> disruptor) {
		this.disruptor = disruptor;
	}

	@Override
	public void handle(Request request) {
		disruptor.publishEvent(new RequestEventTranslator(request));
	}

	class RequestEventTranslator implements EventTranslator<Request> {

		private Request request;

		public RequestEventTranslator(Request request) {
			this.request = request;
		}

		@Override
		public void translateTo(Request request, long sequence) {
			request.setDataInput(this.request.getDataInput());
			request.setSc(this.request.getSc());
			request.setResponse(this.request.getResponse());
			request.setConnector(this.request.getConnector());
		}
	}
}
