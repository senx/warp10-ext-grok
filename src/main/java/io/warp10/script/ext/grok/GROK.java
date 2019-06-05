//
//   Copyright 2019  SenX S.A.S.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package io.warp10.script.ext.grok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;
import io.warp10.script.functions.SNAPSHOT;
import io.warp10.script.functions.SNAPSHOT.Snapshotable;

public class GROK extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public static class WarpScriptGrok implements Snapshotable {
    private final String name;
    private final Grok grok;
    
    public WarpScriptGrok(String name, Grok grok) {
      this.name = name;
      this.grok = grok;
    }
    
    @Override
    public String snapshot() {
      StringBuilder snapshot = new StringBuilder();
      
      try {
        SNAPSHOT.addElement(snapshot, this.grok.getOriginalGrokPattern());
        SNAPSHOT.addElement(snapshot, this.grok.getPatterns());
      } catch (WarpScriptException wse) {
        throw new RuntimeException(wse);
      }
      snapshot.append(" ");
      snapshot.append(this.name);

      return snapshot.toString();
    }

    public Grok getGrok() {
      return grok;
    }        
  }
  
  public GROK(String name) {
    super(name);
  }

  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    Object top = stack.pop();

    Map<String,String> registered = null;
    boolean loadDefault = false;
    
    if (top instanceof Map) {
      registered = new HashMap<String,String>(((Map) top).size());
      for (Entry<Object,Object> entry: ((Map<Object,Object>) top).entrySet()) {
        if (null == entry.getKey()) {
          loadDefault = true;
          continue;
        }
        if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof String)) {
          throw new WarpScriptException(getName() + " invalid registered patterns, expects a MAP of STRING to STRING.");
        }
        registered.put(entry.getKey().toString(), entry.getValue().toString());
      }
      top = stack.pop();
    }
    
    if (!(top instanceof String) && !(top instanceof List)) {
      throw new WarpScriptException(getName() + " operates on a Grok pattern (a STRING) or a LIST thereof.");
    }
    
    GrokCompiler compiler = GrokCompiler.newInstance();
    
    if (loadDefault || null == registered || registered.isEmpty()) {
      compiler.registerDefaultPatterns();
    }
    
    if (null != registered && !registered.isEmpty()) {
      compiler.register(registered);
    }

    if (top instanceof String) {
      Grok grok = compiler.compile(top.toString());
      stack.push(new WarpScriptGrok(getName(), grok));
    } else if (top instanceof String) {
      List<Object> groks = new ArrayList<Object>(((List) top).size());
      for (Object elt: (List) top) {
        if (!(elt instanceof String)) {
          throw new WarpScriptException(getName() + " operates on a LIST of Grok patterns (STRINGs).");
        }
        groks.add(compiler.compile(elt.toString()));
      }
      stack.push(groks);
    }
    
    return stack;
  }
}
