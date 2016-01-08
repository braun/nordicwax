package lw.nordicwax;

import lw.droid.commons.Helper;

public class ActivityType {

	public ActivityType(String title) {
		super();
		this.title = title;
	}
	
		String title;

		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

	
		public boolean empty()
		{
			return Helper.isStringEmpty(title);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if(this == o)
				return true;
			
			if(!(o instanceof SnowType))
				return false;
			
			SnowType other = (SnowType) o;
			if(Helper.isStringEmpty(other.getTitle()))
			{
				if(Helper.isStringEmpty(getTitle()))
					return true;
				return false;
			}
			
			return other.getTitle().equals(getTitle());
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int result = 666;
			result += 9*result+title.hashCode();
			return result;
		}
	

}
