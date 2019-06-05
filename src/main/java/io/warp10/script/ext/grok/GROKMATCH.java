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

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.Match;
import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;
import io.warp10.script.ext.grok.GROK.WarpScriptGrok;

public class GROKMATCH extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  public GROKMATCH(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    Object top = stack.pop();
    
    if (!(top instanceof WarpScriptGrok)) {
      throw new WarpScriptException(getName() + " expects a Grok instance.");
    }
    
    Grok grok = ((WarpScriptGrok) top).getGrok();
    
    top = stack.pop();
    
    if (!(top instanceof String)) {
      throw new WarpScriptException(getName() + " operates on a STRING.");
    }
    
    Match m = grok.match(top.toString());
    
    stack.push(m.capture());
    
    return stack;
  }

}
