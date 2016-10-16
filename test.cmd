
command home {

	set [string:name] {
		[int:x] [int:y] [int:z] {
			run home_set_coords name x y z;
			perm home.set.xyz;
			help Sets a new home at coordinates XYZ;
		}
		run home_set name;
		perm home.set;
		help Sets a new home;
		type none;
	}
	del [optional:-a] [string:name] {
		run home_del name -a;
		help Deletes a home\n&cCannot be undone!;
		perm home.del;
	}
	list {
		help Shows all homes;
		run home_list;
		perm home.list;
	}
	[string:name] {
		perm home.tp;
		help Teleports to a home;
		run home_tp name;
	}
	yolo swag {
		perm yo.mamma;
		help Reks you;
		run noskope;
	}
}