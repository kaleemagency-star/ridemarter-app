package com.smartdrivo.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.smartdrivo.app.model.DateFilter
import com.smartdrivo.app.model.HistoryStats
import com.smartdrivo.app.model.OrderHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    constructor() : this(Application())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _historyList = MutableStateFlow<List<OrderHistory>>(emptyList())
    val historyList: StateFlow<List<OrderHistory>> = _historyList.asStateFlow()

    private val _filteredList = MutableStateFlow<List<OrderHistory>>(emptyList())
    val filteredList: StateFlow<List<OrderHistory>> = _filteredList.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilter.TODAY)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlatforms: StateFlow<Set<String>> = _selectedPlatforms.asStateFlow()

    private val _selectedActions = MutableStateFlow<Set<String>>(emptySet())
    val selectedActions: StateFlow<Set<String>> = _selectedActions.asStateFlow()

    private val _stats = MutableStateFlow(HistoryStats())
    val stats: StateFlow<HistoryStats> = _stats.asStateFlow()

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val db: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private var historyListener: ListenerRegistration? = null

    init {
        loadHistory()
    }

    override fun onCleared() {
        super.onCleared()
        historyListener?.remove()
        historyListener = null
    }

    fun loadHistory() {
        val uid = try {
            auth?.currentUser?.uid
        } catch (e: Exception) {
            null
        }

        if (uid == null || db == null) {
            _historyList.value = getSampleOrders()
            applyFilters()
            return
        }

        _isLoading.value = true

        try {
            historyListener = db.collection("users")
                .document(uid)
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(200)
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null) {
                        Log.w("HistoryVM", "Error: ${error.message}")
                        _historyList.value = getSampleOrders()
                        applyFilters()
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    val orders = snapshot.documents.mapNotNull { doc ->
                        try {
                            val ts = doc.getTimestamp("timestamp")
                            val formatted = formatTimestamp(ts?.toDate())
                            OrderHistory(
                                docId = doc.id,
                                platform = doc.getString("platform") ?: "Unknown",
                                vehicleType = doc.getString("vehicleType") ?: "AUTO",
                                fare = doc.getString("fare") ?: "0",
                                pickupDistance = doc.getString("pickupDistance") ?: "0 km",
                                dropDistance = doc.getString("dropDistance") ?: "0 km",
                                dropArea = doc.getString("dropArea") ?: "Unknown",
                                action = doc.getString("action") ?: "Accepted",
                                speed = doc.getString("speed") ?: "10ms",
                                timestamp = ts,
                                date = formatted.first,
                                time = formatted.second
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    // Remove duplicates by docId
                    val unique = orders.distinctBy { it.docId }
                    _historyList.value = if (unique.isEmpty()) getSampleOrders() else unique
                    applyFilters()
                }
        } catch (e: Exception) {
            Log.w("HistoryVM", "Listener error: ${e.message}")
            _isLoading.value = false
            _historyList.value = getSampleOrders()
            applyFilters()
        }
    }

    fun getSampleOrders(): List<OrderHistory> {
        val now = Calendar.getInstance()
        val today = now.time

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = yesterdayCal.time

        val tsToday = Timestamp(today)
        val tsYesterday = Timestamp(yesterday)

        val formattedToday = formatTimestamp(today)
        val formattedYesterday = formatTimestamp(yesterday)

        return listOf(
            OrderHistory(
                docId = "sample_1",
                platform = "Rapido",
                vehicleType = "AUTO",
                fare = "142",
                pickupDistance = "0.8 km",
                dropDistance = "5.4 km",
                dropArea = "Indiranagar Metro",
                action = "Accepted",
                speed = "10ms",
                timestamp = tsToday,
                date = formattedToday.first,
                time = formattedToday.second
            ),
            OrderHistory(
                docId = "sample_2",
                platform = "Uber",
                vehicleType = "MOTO",
                fare = "85",
                pickupDistance = "1.2 km",
                dropDistance = "3.1 km",
                dropArea = "Koramangala 4th Block",
                action = "Accepted",
                speed = "15ms",
                timestamp = tsToday,
                date = formattedToday.first,
                time = formattedToday.second
            ),
            OrderHistory(
                docId = "sample_3",
                platform = "Ola",
                vehicleType = "CAB",
                fare = "320",
                pickupDistance = "3.5 km",
                dropDistance = "12.0 km",
                dropArea = "Electronic City Phase 1",
                action = "Rejected",
                speed = "20ms",
                timestamp = tsToday,
                date = formattedToday.first,
                time = formattedToday.second
            ),
            OrderHistory(
                docId = "sample_4",
                platform = "Rapido",
                vehicleType = "AUTO",
                fare = "65",
                pickupDistance = "2.1 km",
                dropDistance = "1.8 km",
                dropArea = "HSR Layout Sector 2",
                action = "Ignored",
                speed = "10ms",
                timestamp = tsToday,
                date = formattedToday.first,
                time = formattedToday.second
            ),
            OrderHistory(
                docId = "sample_5",
                platform = "Uber",
                vehicleType = "AUTO",
                fare = "195",
                pickupDistance = "0.5 km",
                dropDistance = "7.2 km",
                dropArea = "Whitefield Main Road",
                action = "Accepted",
                speed = "10ms",
                timestamp = tsYesterday,
                date = formattedYesterday.first,
                time = formattedYesterday.second
            ),
            OrderHistory(
                docId = "sample_6",
                platform = "Ola",
                vehicleType = "AUTO",
                fare = "110",
                pickupDistance = "1.0 km",
                dropDistance = "4.0 km",
                dropArea = "MG Road Brigade Junction",
                action = "Accepted",
                speed = "10ms",
                timestamp = tsYesterday,
                date = formattedYesterday.first,
                time = formattedYesterday.second
            ),
            OrderHistory(
                docId = "sample_7",
                platform = "Rapido",
                vehicleType = "MOTO",
                fare = "55",
                pickupDistance = "1.9 km",
                dropDistance = "2.5 km",
                dropArea = "BTM Layout 2nd Stage",
                action = "Rejected",
                speed = "25ms",
                timestamp = tsYesterday,
                date = formattedYesterday.first,
                time = formattedYesterday.second
            ),
            OrderHistory(
                docId = "sample_8",
                platform = "Uber",
                vehicleType = "CAB",
                fare = "480",
                pickupDistance = "4.2 km",
                dropDistance = "18.5 km",
                dropArea = "Kempegowda Airport T1",
                action = "Ignored",
                speed = "10ms",
                timestamp = tsYesterday,
                date = formattedYesterday.first,
                time = formattedYesterday.second
            )
        )
    }

    fun applyFilters() {
        var result = _historyList.value

        // Date filter
        result = when (_selectedDateFilter.value) {
            DateFilter.TODAY -> result.filter { order ->
                order.timestamp?.toDate()?.let { date ->
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val today = Calendar.getInstance()
                    cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                } ?: true
            }
            DateFilter.YESTERDAY -> result.filter { order ->
                order.timestamp?.toDate()?.let { date ->
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
                } ?: true
            }
            DateFilter.LAST_7_DAYS -> result.filter { order ->
                order.timestamp?.toDate()?.let { date ->
                    val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                    date.after(sevenDaysAgo.time)
                } ?: true
            }
            DateFilter.LAST_30_DAYS -> result.filter { order ->
                order.timestamp?.toDate()?.let { date ->
                    val thirtyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
                    date.after(thirtyDaysAgo.time)
                } ?: true
            }
            DateFilter.ALL_TIME -> result
        }

        // Platform filter
        if (_selectedPlatforms.value.isNotEmpty()) {
            result = result.filter {
                it.platform in _selectedPlatforms.value
            }
        }

        // Action filter
        if (_selectedActions.value.isNotEmpty()) {
            result = result.filter {
                it.action in _selectedActions.value
            }
        }

        _filteredList.value = result
        updateStats(result)
    }

    fun updateStats(orders: List<OrderHistory>) {
        _stats.value = HistoryStats(
            total = orders.size,
            accepted = orders.count { it.action == "Accepted" },
            rejected = orders.count { it.action == "Rejected" },
            ignored = orders.count { it.action == "Ignored" }
        )
    }

    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
        applyFilters()
    }

    fun togglePlatformFilter(platform: String) {
        if (platform == "All") {
            _selectedPlatforms.value = emptySet()
        } else {
            val current = _selectedPlatforms.value.toMutableSet()
            if (platform in current) current.remove(platform)
            else current.add(platform)
            _selectedPlatforms.value = current
        }
        applyFilters()
    }

    fun toggleActionFilter(action: String) {
        if (action == "All") {
            _selectedActions.value = emptySet()
        } else {
            val current = _selectedActions.value.toMutableSet()
            if (action in current) current.remove(action)
            else current.add(action)
            _selectedActions.value = current
        }
        applyFilters()
    }

    fun refreshHistory() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val uid = auth?.currentUser?.uid
                if (uid == null || db == null) {
                    _historyList.value = getSampleOrders()
                    applyFilters()
                    return@launch
                }
                val snapshot = db.collection("users")
                    .document(uid)
                    .collection("history")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(200)
                    .get()
                    .await()
                val orders = snapshot.documents.mapNotNull { doc ->
                    try {
                        val ts = doc.getTimestamp("timestamp")
                        val formatted = formatTimestamp(ts?.toDate())
                        OrderHistory(
                            docId = doc.id,
                            platform = doc.getString("platform") ?: "Unknown",
                            vehicleType = doc.getString("vehicleType") ?: "AUTO",
                            fare = doc.getString("fare") ?: "0",
                            pickupDistance = doc.getString("pickupDistance") ?: "0 km",
                            dropDistance = doc.getString("dropDistance") ?: "0 km",
                            dropArea = doc.getString("dropArea") ?: "Unknown",
                            action = doc.getString("action") ?: "Accepted",
                            speed = doc.getString("speed") ?: "10ms",
                            timestamp = ts,
                            date = formatted.first,
                            time = formatted.second
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _historyList.value = if (orders.isEmpty()) getSampleOrders() else orders.distinctBy { it.docId }
                applyFilters()
            } catch (e: Exception) {
                Log.w("HistoryVM", "Refresh error: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun formatTimestamp(date: Date?): Pair<String, String> {
        if (date == null) return Pair("Today", "Just now")
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return Pair(dateFormat.format(date), timeFormat.format(date))
    }
}
