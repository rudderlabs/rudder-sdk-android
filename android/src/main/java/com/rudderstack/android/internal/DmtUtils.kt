package com.rudderstack.android.internal


/**
 * [STATUS_NEW] should be the initial status.
 * Any further operations should be masked with appropriate status
 * for eg:
 * for setting cloud mode done the way should be
  ```
    var status = STATUS_NEW
    status = status maskWith STATUS_CLOUD_MODE_DONE
  ```
 */
internal const val STATUS_NEW = 0
internal const val STATUS_CLOUD_MODE_DONE = 2
internal const val STATUS_DEVICE_MODE_DONE = 1

infix fun Int.maskWith(status: Int) : Int{
    return this or (1 shl status)
}
infix fun Int.unmaskWith(status: Int) : Int{
    return this and (1 shl status).inv()
}
fun Int.isDeviceModeDone() : Boolean{
    return this and (1 shl STATUS_DEVICE_MODE_DONE) != 0
}
fun Int.isCloudModeDone() : Boolean{
    return this and (1 shl STATUS_CLOUD_MODE_DONE) != 0
}
