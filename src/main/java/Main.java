import controller.Constants;
import controller.PLC;
import controller.Response;
import jssc.*;

/**
 * Created by kaspar on 23.04.18.
 */
public class Main {

    public static void main(String[] args) throws SerialPortException {

        PLC unitronics = new PLC("/dev/ttyUSB0", Constants.BAUDRATE_115200);

        //System.out.println(unitronics.getBaudRate());

        unitronics.synchronize();
        //System.out.println(code);

        //Response res = unitronics.status();
        //Response res = unitronics.identify();
        //Response res = unitronics.reset();

        Response res = unitronics.stopDownloadRestart();
        res = unitronics.reset();

        String format = String.format("Object: %s, Code: %s", res.getBody(), res.getStatusCode());
        System.out.println(format);


    }
}
