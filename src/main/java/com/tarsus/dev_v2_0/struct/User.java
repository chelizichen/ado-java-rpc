
package com.tarsus.dev_v2_0.struct;

import com.alibaba.fastjson.JSONObject;
import com.tarsus.lib.lib_decorator.struct.TaroStruct;
import com.tarsus.lib.main_control.load_server.TarsusBodyABS;
import com.tarsus.lib.main_control.load_server.impl.TarsusStream;

import java.util.List;


@TaroStruct
public class User extends TarsusBodyABS {
    public String id;
    public String name;
    public String age;
    public String fullName;
    public String address;


    // ListConstructor
    public User(List<?> list) {
        TarsusStream _tarsusStream = new TarsusStream(list, "User");
        this.id = _tarsusStream.read_string(1);
        this.name = _tarsusStream.read_string(2);
        this.age = _tarsusStream.read_string(3);
        this.fullName = _tarsusStream.read_string(4);
        this.address = _tarsusStream.read_string(5);

    }

    // NoArgsConstructor
    public User() {
    }

    // toJson
    @Override
    public String json() {
        Object o = JSONObject.toJSON(this);
        return o.toString();
    }
}
