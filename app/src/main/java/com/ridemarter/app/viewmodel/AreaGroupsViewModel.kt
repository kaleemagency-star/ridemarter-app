package com.ridemarter.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ridemarter.app.dataStore
import com.ridemarter.app.model.AreaGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

class AreaGroupsViewModel(application: Application) : AndroidViewModel(application) {

    constructor() : this(Application())

    private val KEY_GLOBAL_MIN_PICKUP = floatPreferencesKey("global_min_pickup")

    private val _gotoGroups = MutableStateFlow<List<AreaGroup>>(emptyList())
    val gotoGroups: StateFlow<List<AreaGroup>> = _gotoGroups.asStateFlow()

    private val _noGoGroups = MutableStateFlow<List<AreaGroup>>(emptyList())
    val noGoGroups: StateFlow<List<AreaGroup>> = _noGoGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _globalMinPickup = MutableStateFlow(0.5f)
    val globalMinPickup: StateFlow<Float> = _globalMinPickup.asStateFlow()

    private val _showAddGroupDialog = MutableStateFlow(false)
    val showAddGroupDialog: StateFlow<Boolean> = _showAddGroupDialog.asStateFlow()

    private val _newGroupName = MutableStateFlow("")
    val newGroupName: StateFlow<String> = _newGroupName.asStateFlow()

    private val _newGroupType = MutableStateFlow("GOTO")
    val newGroupType: StateFlow<String> = _newGroupType.asStateFlow()

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

    private var gotoListener: ListenerRegistration? = null
    private var noGoListener: ListenerRegistration? = null

    init {
        loadGroups()
        loadGlobalSettings()
    }

    override fun onCleared() {
        super.onCleared()
        gotoListener?.remove()
        gotoListener = null
        noGoListener?.remove()
        noGoListener = null
    }

    fun loadGroups() {
        val uid = try {
            auth?.currentUser?.uid
        } catch (e: Exception) {
            null
        }

        if (uid == null || db == null) {
            _gotoGroups.value = getSampleGotoGroups()
            _noGoGroups.value = getSampleNoGoGroups()
            return
        }

        _isLoading.value = true

        try {
            gotoListener = db.collection("users").document(uid)
                .collection("areaGroups")
                .whereEqualTo("type", "GOTO")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null) {
                        Log.w("AreaGroupsVM", "GOTO listener error: ${error.message}")
                        if (_gotoGroups.value.isEmpty()) {
                            _gotoGroups.value = getSampleGotoGroups()
                        }
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val groups = snapshot.documents.mapNotNull { docToAreaGroup(it) }
                        _gotoGroups.value = if (groups.isEmpty()) getSampleGotoGroups() else groups
                    }
                }

            noGoListener = db.collection("users").document(uid)
                .collection("areaGroups")
                .whereEqualTo("type", "NOGO")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null) {
                        Log.w("AreaGroupsVM", "NOGO listener error: ${error.message}")
                        if (_noGoGroups.value.isEmpty()) {
                            _noGoGroups.value = getSampleNoGoGroups()
                        }
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val groups = snapshot.documents.mapNotNull { docToAreaGroup(it) }
                        _noGoGroups.value = if (groups.isEmpty()) getSampleNoGoGroups() else groups
                    }
                }
        } catch (e: Exception) {
            Log.w("AreaGroupsVM", "loadGroups error: ${e.message}")
            _isLoading.value = false
            _gotoGroups.value = getSampleGotoGroups()
            _noGoGroups.value = getSampleNoGoGroups()
        }
    }

    private fun getSampleGotoGroups(): List<AreaGroup> {
        return listOf(
            AreaGroup(
                id = "g1",
                name = "City Center",
                isEnabled = true,
                minPickupKm = 0.5f,
                maxDropKm = 6.5f,
                keywords = listOf("MG Road", "Brigade Road", "Cubbon Park"),
                type = "GOTO"
            ),
            AreaGroup(
                id = "g2",
                name = "Airport Area",
                isEnabled = false,
                minPickupKm = 1.0f,
                maxDropKm = 20.0f,
                keywords = listOf("Kempegowda", "Terminal 1", "Terminal 2"),
                type = "GOTO"
            ),
            AreaGroup(
                id = "g3",
                name = "Home Area",
                isEnabled = true,
                minPickupKm = 0.5f,
                maxDropKm = 5.0f,
                keywords = listOf("Indiranagar", "Koramangala"),
                type = "GOTO"
            )
        )
    }

    private fun getSampleNoGoGroups(): List<AreaGroup> {
        return listOf(
            AreaGroup(
                id = "n1",
                name = "Far Suburbs",
                isEnabled = true,
                minPickupKm = 0.5f,
                maxDropKm = 30.0f,
                keywords = listOf("Electronic City", "Whitefield", "Devanahalli"),
                type = "NOGO",
                noGoAction = "REJECT"
            ),
            AreaGroup(
                id = "n2",
                name = "Congestion Zones",
                isEnabled = false,
                minPickupKm = 0.5f,
                maxDropKm = 10.0f,
                keywords = listOf("Silk Board", "KR Puram", "Hebbal"),
                type = "NOGO",
                noGoAction = "IGNORE"
            )
        )
    }

    private fun docToAreaGroup(doc: DocumentSnapshot): AreaGroup? {
        return try {
            AreaGroup(
                id = doc.id,
                name = doc.getString("name") ?: "",
                isEnabled = doc.getBoolean("isEnabled") ?: (doc.getBoolean("enabled") ?: true),
                minPickupKm = (doc.getDouble("minPickupKm") ?: 0.5).toFloat(),
                maxDropKm = (doc.getDouble("maxDropKm") ?: 6.5).toFloat(),
                keywords = (doc.get("keywords") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                type = doc.getString("type") ?: "GOTO",
                noGoAction = doc.getString("noGoAction") ?: "REJECT"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadGlobalSettings() {
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                val prefs = app.dataStore.data.first()
                val saved = prefs[KEY_GLOBAL_MIN_PICKUP] ?: 0.5f
                _globalMinPickup.value = saved
            } catch (e: Exception) {
                _globalMinPickup.value = 0.5f
            }
        }
    }

    fun saveGlobalMinPickup(km: Float) {
        val rounded = (km * 10f).roundToInt() / 10f
        _globalMinPickup.value = rounded
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.dataStore.edit { prefs ->
                    prefs[KEY_GLOBAL_MIN_PICKUP] = rounded
                }
            } catch (e: Exception) {
                Log.w("AreaGroupsVM", "saveGlobalMinPickup error: ${e.message}")
            }
        }
    }

    fun addGroup(name: String, type: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val newGroup = AreaGroup(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            type = type,
            isEnabled = true,
            minPickupKm = if (type == "GOTO") _globalMinPickup.value else 0.5f,
            maxDropKm = if (type == "GOTO") 6.5f else 30.0f,
            keywords = emptyList(),
            noGoAction = if (type == "NOGO") "REJECT" else "REJECT"
        )

        if (type == "GOTO") {
            _gotoGroups.value = _gotoGroups.value + newGroup
        } else {
            _noGoGroups.value = _noGoGroups.value + newGroup
        }
        _showAddGroupDialog.value = false
        _newGroupName.value = ""

        val uid = auth?.currentUser?.uid ?: return
        try {
            db?.collection("users")?.document(uid)
                ?.collection("areaGroups")
                ?.document(newGroup.id)
                ?.set(newGroup)
        } catch (e: Exception) {
            Log.w("AreaGroupsVM", "addGroup error: ${e.message}")
        }
    }

    fun updateGroup(group: AreaGroup) {
        if (group.type == "GOTO") {
            _gotoGroups.value = _gotoGroups.value.map { if (it.id == group.id) group else it }
        } else {
            _noGoGroups.value = _noGoGroups.value.map { if (it.id == group.id) group else it }
        }

        val uid = auth?.currentUser?.uid ?: return
        try {
            db?.collection("users")?.document(uid)
                ?.collection("areaGroups")
                ?.document(group.id)
                ?.set(group)
        } catch (e: Exception) {
            Log.w("AreaGroupsVM", "updateGroup error: ${e.message}")
        }
    }

    fun deleteGroup(groupId: String) {
        _gotoGroups.value = _gotoGroups.value.filter { it.id != groupId }
        _noGoGroups.value = _noGoGroups.value.filter { it.id != groupId }

        val uid = auth?.currentUser?.uid ?: return
        try {
            db?.collection("users")?.document(uid)
                ?.collection("areaGroups")
                ?.document(groupId)
                ?.delete()
        } catch (e: Exception) {
            Log.w("AreaGroupsVM", "deleteGroup error: ${e.message}")
        }
    }

    fun toggleGroup(group: AreaGroup) {
        val updated = group.copy(isEnabled = !group.isEnabled)
        if (group.type == "GOTO") {
            _gotoGroups.value = _gotoGroups.value.map { if (it.id == group.id) updated else it }
        } else {
            _noGoGroups.value = _noGoGroups.value.map { if (it.id == group.id) updated else it }
        }
        updateGroup(updated)
    }

    fun addKeyword(group: AreaGroup, keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isBlank()) return
        if (group.keywords.contains(trimmed)) return

        val updated = group.copy(keywords = group.keywords + trimmed)
        if (group.type == "GOTO") {
            _gotoGroups.value = _gotoGroups.value.map { if (it.id == group.id) updated else it }
        } else {
            _noGoGroups.value = _noGoGroups.value.map { if (it.id == group.id) updated else it }
        }

        val uid = auth?.currentUser?.uid ?: return
        try {
            db?.collection("users")?.document(uid)
                ?.collection("areaGroups")
                ?.document(group.id)
                ?.update("keywords", FieldValue.arrayUnion(trimmed))
        } catch (e: Exception) {
            Log.w("AreaGroupsVM", "addKeyword error: ${e.message}")
        }
    }

    fun removeKeyword(group: AreaGroup, keyword: String) {
        if (keyword.isBlank()) return
        val updated = group.copy(keywords = group.keywords.filter { it != keyword })
        if (group.type == "GOTO") {
            _gotoGroups.value = _gotoGroups.value.map { if (it.id == group.id) updated else it }
        } else {
            _noGoGroups.value = _noGoGroups.value.map { if (it.id == group.id) updated else it }
        }

        val uid = auth?.currentUser?.uid ?: return
        try {
            db?.collection("users")?.document(uid)
                ?.collection("areaGroups")
                ?.document(group.id)
                ?.update("keywords", FieldValue.arrayRemove(keyword))
        } catch (e: Exception) {
            Log.w("AreaGroupsVM", "removeKeyword error: ${e.message}")
        }
    }

    fun showAddDialog(type: String) {
        _newGroupType.value = type
        _showAddGroupDialog.value = true
    }

    fun hideAddDialog() {
        _showAddGroupDialog.value = false
        _newGroupName.value = ""
    }

    fun onNewGroupNameChange(name: String) {
        _newGroupName.value = name
    }
}
