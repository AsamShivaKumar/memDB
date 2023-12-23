package resp;

public class Value{
		private String type;
		private String string;
		private String bulk;
		private int num;
		private Value array[];
		
		public Value(String type){
			this.type = type;
		}
		
		// setters
		public boolean setString(String string) {
			if(!type.equals("string")) return false;
			this.string = string;
			return true;
		}
		
		public boolean setBulk(String bulk) {
			if(!type.equals("bulk")) return false;
			this.bulk = bulk;
			return true;
		}
		
		public boolean setNum(int num) {
			if(!type.equals("num")) return false;
			this.num = num;
			return true;
		}	
		
		public boolean setArray(Value array[]) {
			if(!type.equals("array")) return false;
			this.array = array.clone();
			return true;
		}
		
		// getters
        public String getType() {
            return type;
        }

		public String getString() {
			if(!type.equals("string")) return null;
			return string;
		}
		
		public String getBulk() {
			if(!type.equals("bulk")) return null;
			return bulk;
		}
		
		public Integer getNum() {
			if(!type.equals("num")) return null;
			return num;
		}	
		
		public Value[] getArray() {
			// System.out.println(type + " type " + serializeArray());
			if(!type.equals("array")) return null;
			return array.clone();
		}
		
		public String toString() {
			return "Value{" + 
					"type: " + type + 
					getValue();
		}


        public String serializeValue() {
            switch(type) {
                case "string": return "+" + string + "\r\n";
                case "bulk": return "$" + bulk.length() + "\r\n" + bulk + "\r\n";
                case "num": return ":" + num + "\r\n";
                case "array": return "*" + array.length + "\r\n" + serializeArray();
                default: return "";
            }
        }

        private String serializeArray(){
            StringBuilder str = new StringBuilder("");

            for(int i = 0; i < array.length; i++)
                str.append(array[i].serializeValue());
            
            return str.toString();
        }
		
		private String getValue() {
			
			switch(type) {
				case "string": return ",string: " + string + "}";
				case "bulk": return ",bulk: " + bulk + "}";
				case "num": return ",num: " + num + "}";
				case "array": {
					String ret = ",array: [\n";
					
					for(int i = 0; i < array.length; i++) ret += array[i].toString() + ",\n";
					
					ret += "]}";
					return ret;
				}
				default: return "";
			}
		}
		
	}