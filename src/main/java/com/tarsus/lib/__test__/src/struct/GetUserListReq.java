
package com.tarsus.lib.__test__.src.struct;

import com.alibaba.fastjson.JSONObject;
import com.tarsus.example.base.inf.TarsusJson;
import com.tarsus.lib.decorator.struct.TaroStruct;
import com.tarsus.lib.main_control.load_server.TarsusJsonInf;
import com.tarsus.lib.main_control.load_server.impl.TarsusStream;


import java.util.List;


@TaroStruct
public class GetUserListReq implements TarsusJson {
    public String text;
    public Basic basic;
    public List<Integer> ids;


    // ListConstructor
    public GetUserListReq(List<?> list) {
        TarsusStream _tarsusStream = new TarsusStream(list, "GetUserListReq");
        this.basic = _tarsusStream.read_struct(1, "Basic");
        this.ids = _tarsusStream.read_list(2, "List<int>");

    }

    // NoArgsConstructor
    public GetUserListReq() {

    }

    // toJson
    @Override
    public String json() {
        Object o = JSONObject.toJSON(this);
        return o.toString();
    }
}
