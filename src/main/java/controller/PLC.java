package controller;

import extractor.MessageExtractor;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

import java.util.Arrays;

import static controller.Constants.*;
import static controller.Messages.*;
import static controller.Response.StatusCode;

/**
 * Created by kaspar on 29.04.18.
 */
public class PLC {

    private final SerialPort port;
    private final int baudrate;
    private MessageExtractor extractor = c -> "[EXTRACTED] - " + c;

    public PLC(final String port, final int baudrate) throws SerialPortException {
        this.baudrate = baudrate;
        this.port = new SerialPort(port);
    }

    private StatusCode startCommunication() {
        return startCommunication(baudrate);
    }

    private StatusCode startCommunication(int baudrate) {
        try {
            port.openPort();
            this.port.setParams(baudrate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            return StatusCode.OK;
        } catch (SerialPortException e) {
            return StatusCode.ERROR;
        }
    }

    private StatusCode endCommunication() {
        StatusCode code = (port.isOpened()) ? StatusCode.ERROR : StatusCode.OK;
        try {
            port.closePort();
        } catch (SerialPortException e) {
            code = StatusCode.ERROR;
        }
        return code;
    }

    private Response sendAndReceive(int[] message, int expectedResponseLength, MessageExtractor function) {
        StatusCode code = StatusCode.OK;
        String result = null;
        try {
            port.writeIntArray(message);
            try {
                result = port.readHexString(expectedResponseLength, ", ", 1000);
                port.readHexString();
                //TODO: vb tuleks buffertyhjaks teha juhul kui length > estimated
                result = function.extract(result);
            } catch (SerialPortTimeoutException e) {
                code = StatusCode.TIMEOUT;
            }
        } catch (SerialPortException e) {
            code = StatusCode.ERROR;
        }
        return new Response<>(result, code);
    }

    public int getBaudRate() {
        int[] choices = { BAUDRATE_2400, BAUDRATE_4800, BAUDRATE_9600, BAUDRATE_19200, BAUDRATE_38400, BAUDRATE_57600, BAUDRATE_115200 };
        for (int i = 0; i < choices.length; i++) {
            startCommunication(choices[i]);
            Response response = sendAndReceive(Messages.IDENTIFY, Messages.IDENTIFY_RESPONSE_LEN, extractor);
            endCommunication();
            if (response.getStatusCode() == StatusCode.OK) return choices[i];
        }
        return -1;
    }

    public Response setBaudrate(int oldBaudrate, int newBaudrate) {
        Integer[] choices = { BAUDRATE_2400, BAUDRATE_4800, BAUDRATE_9600, BAUDRATE_19200, BAUDRATE_38400, BAUDRATE_57600, BAUDRATE_115200 };
        int[][] messages = { B2400, B4800, B9600, B19200, B38400, B57600, B115200};
        int idx = Arrays.asList(choices).indexOf(newBaudrate);
        if (idx == -1) return new Response<String>(null, StatusCode.ERROR);
        startCommunication(oldBaudrate);
        Response response1 = sendAndReceive(messages[idx], Messages.BOK.length, extractor);
        endCommunication();
        return response1;
    }

    public void synchronize() {
        int realBaudRate = getBaudRate();
        if (baudrate == realBaudRate || realBaudRate == -1) return;
        setBaudrate(realBaudRate, baudrate);
    }

    public Response reset() {
        startCommunication();
        Response response1 = sendAndReceive(Messages.IDENTIFY, Messages.IDENTIFY_RESPONSE_LEN, extractor);
        Response response2 = sendAndReceive(Messages.RESET, Messages.OK.length, extractor);
        endCommunication();
        return response2;
    }

    /** OPLC */
    public Response identify() {
        startCommunication();
        Response response1 = sendAndReceive(Messages.IDENTIFY, Messages.IDENTIFY_RESPONSE_LEN, extractor);
        endCommunication();
        return response1;
    }

    public Response run() {
        startCommunication();
        Response response1 = sendAndReceive(Messages.IDENTIFY, Messages.IDENTIFY_RESPONSE_LEN, extractor);
        Response response2 = sendAndReceive(Messages.RUN, Messages.OK.length, extractor);
        endCommunication();
        return response2;
    }

    public Response stop() {
        startCommunication();
        Response response1 = sendAndReceive(Messages.IDENTIFY, Messages.IDENTIFY_RESPONSE_LEN, extractor);
        Response response2 = sendAndReceive(Messages.STOP, Messages.OK.length, extractor);
        endCommunication();
        return response2;
    }

    public Response status() {
        startCommunication();
        Response response1 = sendAndReceive(Messages.IDENTIFY, Messages.IDENTIFY_RESPONSE_LEN, extractor);
        Response response2 = sendAndReceive(Messages.PLC_STATUS, Messages.PLC_STATUS_RESPONSE_LEN, extractor);
        endCommunication();
        return response2;
    }

    public Response stopDownloadRestart() {
        Messages.readMessages();
        startCommunication();
        Response response = null;
        for (int i = 0; i < Messages.NUM_OF_REQUESTS; i++) {
            response = sendAndReceive(Messages.REQUESTS[i], Messages.RESPONSES[i].length, extractor);
            if (response.getStatusCode() != StatusCode.OK) {
                System.out.println("[ERROR] - error with request no " + i + ": " + response.getStatusCode());
                String format = String.format("%d : %d", ((String) response.getBody()).split(", ").length, Messages.RESPONSES[i].length);
                System.out.println(format);
                break;
            }
            // TODO: wait a little and compare response with Messages.RESPONSES[i]
        }
        endCommunication();
        return response;
    }





}
