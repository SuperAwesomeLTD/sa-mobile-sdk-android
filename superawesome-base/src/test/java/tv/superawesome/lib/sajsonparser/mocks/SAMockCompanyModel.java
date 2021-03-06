package tv.superawesome.lib.sajsonparser.mocks;

import org.json.JSONObject;

import java.util.List;

import tv.superawesome.lib.sajsonparser.SABaseObject;
import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.sajsonparser.SAJsonToList;

/**
 * Created by gabriel.coman on 18/10/16.
 */
public class SAMockCompanyModel extends SABaseObject {
    public String name;
    public List<SAMockEmployeeModel> employees;

    public SAMockCompanyModel(String name, List<SAMockEmployeeModel> employees) {
        this.name = name;
        this.employees = employees;
    }

    public SAMockCompanyModel(JSONObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public void readFromJson(JSONObject json) {
        name = SAJsonParser.getString(json, "name");
        employees = SAJsonParser.getListFromJsonArray(json, "employees", (SAJsonToList<SAMockEmployeeModel, JSONObject>) SAMockEmployeeModel::new);
    }

    @Override
    public JSONObject writeToJson() {
        return SAJsonParser.newObject("name", name,
                "employees", SAJsonParser.getJsonArrayFromList(employees, SAMockEmployeeModel::writeToJson));
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
