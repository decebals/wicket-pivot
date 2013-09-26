/*
 * Copyright 2012 Decebal Suiu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.wicket.pivot;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public abstract class Aggregator implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String SUM = "sum";
	public static final String AVG = "avg";
	public static final String MIN = "min";
	public static final String MAX = "max";
	public static final String COUNT = "count";
	
	public static final List<String> FUNCTIONS = Collections.unmodifiableList(Arrays.asList(SUM, AVG, MIN, MAX, COUNT));
		
	public static List<String> getFunctions() {		
		return FUNCTIONS;
	}
	
    public static Aggregator get(String function) {
        if (function.equalsIgnoreCase(SUM)) {
        	return new Sum();
        } else if (function.equalsIgnoreCase(AVG)) {
        	return new Average();
        } else if (function.equalsIgnoreCase(MIN)) {
        	return new Minimum();
        } else if (function.equalsIgnoreCase(MAX)) {
        	return new Maximum();
        } else if (function.equalsIgnoreCase(COUNT)) {
        	return new Count();
        }
        
        return null;
    }

    public abstract Aggregator init();
    
    public abstract Aggregator add(Object value);    

    public abstract Object getResult();
    
    public abstract String getFunction();
    
	public Aggregator addAll(Object... values) {
		for (Object value : values) {
			add(value);
		}
		
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Aggregator) {
			String tmp = ((Aggregator) obj).getFunction();
			return getFunction().equals(tmp);
		}
		
		return false;
	}

    public static class Sum extends Aggregator {
    	
        private static final long serialVersionUID = 1L;
        
		private double total;

		@Override
		public Aggregator init() {
			total = 0.0;
			
			return this;
		}

        @Override
		public Aggregator add(Object value) {
            if (value instanceof Number) {
            	total += ((Number) value).doubleValue();
            }
            
            return this;
        }

        @Override
		public Object getResult() {
            return new Double(total);
        }

		@Override
		public String getFunction() {
			return SUM;
		}
        
    }

    public static class Average extends Aggregator {
    	
        private static final long serialVersionUID = 1L;
        
        private double total;
        private double count;

		@Override
		public Aggregator init() {
			total = 0.0;
			count = 0.0;
			
			return this;
		}

        @Override
		public Aggregator add(Object value) {
            if (value instanceof Number) {
                total += ((Number) value).doubleValue();
                count++;
            }
            
            return this;
        }

        @Override
		public Object getResult() {
            if (count == 0.0) {
            	return null;
            }
            
            return new Double(total / count);
        }

		@Override
		public String getFunction() {
			return AVG;
		}
        
    }

    public static class Minimum extends Aggregator {
    	
        private static final long serialVersionUID = 1L;
        
        private Object min;

		@Override
		public Aggregator init() {
			min = null;
			
			return this;
		}

        @Override
		@SuppressWarnings("unchecked")
		public Aggregator add(Object value) {
            if (value != null) {
                if (min == null) {
                	min = value;
                } else if (value instanceof Comparable) {
                    if (((Comparable<Object>) value).compareTo(min) < 0) {
                    	min = value;
                    }
                }
            }
            
            return this;
        }

        @Override
		public Object getResult() {
            return min;
        }

		@Override
		public String getFunction() {
			return MIN;
		}
        
    }

    public static class Maximum extends Aggregator {
    	
        private static final long serialVersionUID = 1L;
        
        private Object max;

		@Override
		public Aggregator init() {
			max = null;
			
			return this;
		}

        @Override
		@SuppressWarnings("unchecked")
		public Aggregator add(Object value) {
            if (value != null) {
                if (max == null) {
                	max = value;
                } else if (value instanceof Comparable) {
                    if (((Comparable<Object>) value).compareTo(max) > 0) {
                    	max = value;
                    }
                }
            }
            
            return this;
        }

        @Override
		public Object getResult() {
            return max;
        }

		@Override
		public String getFunction() {
			return MAX;
		}
        
    }

    public static class Count extends Aggregator {
    	
        private static final long serialVersionUID = 1L;
        
        private int count;

		@Override
		public Aggregator init() {
			count = 0;
			
			return this;
		}

        @Override
		public Aggregator add(Object value) {
            if (value != null) {
            	count++;
            }
            
            return this;
        }

        @Override
		public Object getResult() {
            return new Integer(count);
        }

		@Override
		public String getFunction() {
			return COUNT;
		}
        
    }
    
}
