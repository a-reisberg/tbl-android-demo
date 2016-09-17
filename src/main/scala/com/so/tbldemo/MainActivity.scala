package com.so.tbldemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.couchbase.lite.Manager
import com.couchbase.lite.android.AndroidContext
import com.shalloui.tblite._
import com.shalloui.tblite.TblQuery._
import shapeless._

class MainActivity extends AppCompatActivity {

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    val manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS)
    manager.setStorageType("ForestDB")
    manager.getDatabase("my-database").delete()
    val db = manager.getDatabase("my-database")

    val tblDb = TblDb[Company](db)

    // Create some random addresses
    val ny1 = Address("New York", "12345")
    val ny2 = Address("New York", "13245")
    val chicago = Address("Chicago", "43254")
    val sf = Address("San Francisco", "13324")

    // and some random employees
    val john = Employee("John Doe", 35, ny1)
    val jamie = Employee("Jamie Saunders", 25, ny2)
    val bradford = Employee("Bradford Newton", 30, chicago)
    val tina = Employee("Tina Rivera", 23, sf)
    val whitney = Employee("Whitney Perez", 40, sf)

    // Now add employees to the db
    val johnId = tblDb.put(john)
    val jamieId = tblDb.put(jamie)
    val bradfordId = tblDb.put(bradford)
    val tinaId = tblDb.put(tina)
    val whitneyId = tblDb.put(whitney)

    // Now create the departments
    val saleDept = Department("Sale", List(johnId, jamieId))
    val hr = Department("HR", List(bradfordId))
    val customerSupp = Department("Customer Support", List(tinaId, whitneyId))

    // Now insert departments to the Db
    val saleDeptId = tblDb.put(saleDept)
    val hrId = tblDb.put(hr)
    val customerSuppId = tblDb.put(customerSupp)

    logSection("Get sale dept and hr dept")
    tblLog(tblDb.getType[Department](saleDeptId, hrId))

    // Query all departments.
    // typeView is a view created automatically by typebase lite.
    // deptQ just build the query, and doesn't run it yet. So one can reuse it in the future.
    val deptQ = tblDb.typeView[Department]

    // Now, run the query.
    logSection("dept")
    deptQ.foreach(tblLog)

    // For comprehension also works
    logSection("Or by for comprehension")
    for (dept <- deptQ) tblLog(dept)

    // A bit more complex query: query all department names along with list of employees name
    // deptQ was defined earlier, i.e. one can reuse it and compose with other queries using
    // various combinators.
    logSection("Department name, along with List of employee names")
    val deptEmployeeQ = for {
      dept <- deptQ
      employeeNames = tblDb.getType[Employee](dept.employeeIds: _*).map(_.name)
    } yield (dept.name, employeeNames)

    deptEmployeeQ.foreach(tblLog)

    // Now, we want to create a more complex query: everyone who lives whose age is > 30 and lives in NYC
    logSection("Everyone whose age is > 30 and is living in New York")
    val cityAndAgeQ = for {
      employee <- tblDb.typeView[Employee].filter(e => (e.age > 30) && (e.address.city == "New York"))
    } yield employee

    cityAndAgeQ.foreach(tblLog)

    // But that was inefficient, because it has to loop through everyone.
    // Would it be nicer to have an index if we do this query often? Enter View!

    // First, create TblView. The key of the index is String (for city) and Int (for age).
    // More general MapViews and also be created via createMapView. Map-Reduce will come soon.
    logSection("Same query, but with index")
    val cityAgeIndex = tblDb.createIndex[String :: Int :: HNil]("city-age", "1.0", {
      case e: Employee => Set(e.address.city :: e.age :: HNil)
      case _ => Set()
    })

    // Now, we create a query using the index. This Query can also be mixed with others, using various combinators.
    val cityAgeQ2 = cityAgeIndex.sQuery(startKey("New York" :: 30 :: HNil), endKey("New York" :: Last)).extractType[Employee]

    cityAgeQ2.foreach(tblLog)

    // Live queries are also supported. Now we want to be notified
    // whenever someone from New York, whose age is > 30 starts at our company.
    logSection("Live query: anyone new of age > 30 from NY?")
    val liveQ = cityAgeIndex.sLiveQuery(startKey("New York" :: 30 :: HNil), endKey("New York" :: Last)).extractType[Employee]
    val subscription = liveQ.subscribe(_.foreach(tblLog))
    liveQ.start()

    tblDb.put(Employee("New Comer", 31, Address("New York", "99999")))
  }

  def tblLog(s: AnyRef): Unit = {
    Log.d("typebase-lite", s.toString)
  }

  def logSection(s: String): Unit = {
    tblLog(s"------------ $s ------------")
  }
}