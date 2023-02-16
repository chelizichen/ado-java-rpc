package register;

import base.ArcBaseClass;
import config.ret;
import decorator.ArcMethod;
import decorator.ArcParams;
import decorator.ArcInterFace;
import dto.Job;
import dto.Person;

import java.util.HashMap;

@ArcInterFace(interFace = "HelloInterFace")
public class Hello extends ArcBaseClass {

    @ArcMethod
    public ret TestRet(@ArcParams("Person") Person p1, @ArcParams("Job")Job j1){
        System.out.println("Job Name is ->"+j1.JobName);
        return ret.success(p1);
    }

    @ArcMethod
    public ret say(String args1,String args2){
        HashMap<String, String> hmp = new HashMap();
        hmp.put("d",args1);
        hmp.put("f",args2);
        ret success = ret.success(hmp);
        return success;
    }
}
