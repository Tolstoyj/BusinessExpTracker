package com.dps.businessexpensetracker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.dps.businessexpensetracker.data.Expense
import com.dps.businessexpensetracker.data.ExpenseExport
import com.dps.businessexpensetracker.data.ExpenseExportFormat
import com.dps.businessexpensetracker.data.ExpenseExporter
import com.dps.businessexpensetracker.data.ExpenseCategory
import com.dps.businessexpensetracker.data.ExpenseDraft
import com.dps.businessexpensetracker.data.ExpenseRepository
import com.dps.businessexpensetracker.data.ExpenseStatus
import com.dps.businessexpensetracker.data.PaymentMethod
import com.dps.businessexpensetracker.data.inrCurrencyFormatter
import com.dps.businessexpensetracker.data.validateExpenseDraft
import com.dps.businessexpensetracker.ui.theme.BusinessExpenseTrackerTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusinessExpenseTrackerTheme {
                BusinessExpenseTrackerApp()
            }
        }
    }
}

@Composable
private fun BusinessExpenseTrackerApp() {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(context) }
    var expenses by remember { mutableStateOf(sortedExpenses(repository.loadExpenses())) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var creatingExpense by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Expense?>(null) }

    fun persist(updated: List<Expense>) {
        val sorted = sortedExpenses(updated)
        repository.saveExpenses(sorted)
        expenses = sorted
    }

    when {
        creatingExpense || editingExpense != null -> {
            val initialDraft = editingExpense?.let(ExpenseDraft::fromExpense) ?: ExpenseDraft()
            ExpenseEditorScreen(
                initialDraft = initialDraft,
                onCancel = {
                    creatingExpense = false
                    editingExpense = null
                },
                onSave = { savedExpense ->
                    val withoutPrevious = expenses.filterNot { it.id == savedExpense.id }
                    persist(withoutPrevious + savedExpense)
                    creatingExpense = false
                    editingExpense = null
                }
            )
        }

        else -> {
            ExpenseHomeScreen(
                expenses = expenses,
                onAddExpense = { creatingExpense = true },
                onEditExpense = { editingExpense = it },
                onDeleteExpense = { pendingDelete = it },
                onOpenAttachment = { openAttachment(context, it.attachmentUri) }
            )
        }
    }

    pendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete expense") },
            text = { Text("Delete ${expense.vendor} from the expense register?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        persist(expenses.filterNot { it.id == expense.id })
                        pendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseHomeScreen(
    expenses: List<Expense>,
    onAddExpense: () -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onOpenAttachment: (Expense) -> Unit
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var statusFilterName by rememberSaveable { mutableStateOf("") }
    var categoryFilterName by rememberSaveable { mutableStateOf("") }
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<ExpenseExport?>(null) }
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ExpenseExportFormat.CSV.mimeType),
        onResult = { uri ->
            pendingExport?.let { export -> saveExportToUri(context, uri, export) }
            pendingExport = null
        }
    )
    val htmlExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ExpenseExportFormat.HTML.mimeType),
        onResult = { uri ->
            pendingExport?.let { export -> saveExportToUri(context, uri, export) }
            pendingExport = null
        }
    )
    val currencyFormatter = remember { inrCurrencyFormatter() }
    val selectedStatus = ExpenseStatus.entries.firstOrNull { it.name == statusFilterName }
    val selectedCategory = ExpenseCategory.entries.firstOrNull { it.name == categoryFilterName }
    val filteredExpenses = remember(expenses, query, statusFilterName, categoryFilterName) {
        expenses.filter { expense ->
            val searchText = listOf(
                expense.vendor,
                expense.invoiceNumber,
                expense.submittedBy,
                expense.notes,
                expense.category.label,
                expense.status.label
            ).joinToString(" ").lowercase()
            val matchesSearch = query.isBlank() || searchText.contains(query.trim().lowercase())
            val matchesStatus = selectedStatus == null || expense.status == selectedStatus
            val matchesCategory = selectedCategory == null || expense.category == selectedCategory
            matchesSearch && matchesStatus && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Expense Tracker",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { exportMenuExpanded = true },
                            enabled = expenses.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Export records"
                            )
                        }
                        DropdownMenu(
                            expanded = exportMenuExpanded,
                            onDismissRequest = { exportMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export CSV") },
                                onClick = {
                                    exportMenuExpanded = false
                                    pendingExport = ExpenseExporter.create(
                                        expenses = expenses,
                                        format = ExpenseExportFormat.CSV
                                    )
                                    pendingExport?.let { csvExportLauncher.launch(it.fileName) }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export HTML report") },
                                onClick = {
                                    exportMenuExpanded = false
                                    pendingExport = ExpenseExporter.create(
                                        expenses = expenses,
                                        format = ExpenseExportFormat.HTML
                                    )
                                    pendingExport?.let { htmlExportLauncher.launch(it.fileName) }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Outlined.Add, contentDescription = "Add expense")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                DashboardSummary(
                    expenses = expenses,
                    currencyFormatter = currencyFormatter
                )
            }

            item {
                SearchAndFilters(
                    query = query,
                    onQueryChange = { query = it },
                    statusFilterName = statusFilterName,
                    onStatusFilterChange = { statusFilterName = it },
                    categoryFilterName = categoryFilterName,
                    onCategoryFilterChange = { categoryFilterName = it }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${filteredExpenses.size} records",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredExpenses.isEmpty()) {
                item {
                    EmptyExpenseState(onAddExpense = onAddExpense)
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseListItem(
                        expense = expense,
                        currencyFormatter = currencyFormatter,
                        onEdit = { onEditExpense(expense) },
                        onDelete = { onDeleteExpense(expense) },
                        onOpenAttachment = { onOpenAttachment(expense) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSummary(
    expenses: List<Expense>,
    currencyFormatter: NumberFormat
) {
    val summary = remember(expenses) { ExpenseSummary.from(expenses) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Total spend",
                value = currencyFormatter.format(summary.totalSpend),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "This month",
                value = currencyFormatter.format(summary.thisMonthSpend),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Pending review",
                value = summary.pendingCount.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Paid",
                value = currencyFormatter.format(summary.paidSpend),
                modifier = Modifier.weight(1f)
            )
        }
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Top category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = summary.topCategory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.height(104.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    statusFilterName: String,
    onStatusFilterChange: (String) -> Unit,
    categoryFilterName: String,
    onCategoryFilterChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            label = { Text("Search vendor, invoice, notes") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterMenuButton(
                label = "Status",
                selectedText = ExpenseStatus.entries
                    .firstOrNull { it.name == statusFilterName }
                    ?.label ?: "All",
                options = listOf("" to "All") + ExpenseStatus.entries.map { it.name to it.label },
                onSelected = onStatusFilterChange
            )
            FilterMenuButton(
                label = "Category",
                selectedText = ExpenseCategory.entries
                    .firstOrNull { it.name == categoryFilterName }
                    ?.label ?: "All",
                options = listOf("" to "All") + ExpenseCategory.entries.map { it.name to it.label },
                onSelected = onCategoryFilterChange
            )
        }
    }
}

@Composable
private fun FilterMenuButton(
    label: String,
    selectedText: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $selectedText", maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyExpenseState(onAddExpense: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No expenses recorded",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(onClick = onAddExpense) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add expense")
            }
        }
    }
}

@Composable
private fun ExpenseListItem(
    expense: Expense,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenAttachment: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.vendor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${expense.category.label} • ${formatExpenseDate(expense.date)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(expense.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    StatusBadge(status = expense.status)
                }
            }

            if (expense.invoiceNumber.isNotBlank() || expense.submittedBy.isNotBlank()) {
                Text(
                    text = buildList {
                        if (expense.invoiceNumber.isNotBlank()) add("Invoice ${expense.invoiceNumber}")
                        if (expense.submittedBy.isNotBlank()) add("By ${expense.submittedBy}")
                    }.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (expense.attachmentUri != null) {
                    TextButton(onClick = onOpenAttachment) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = expense.attachmentName ?: "Attachment",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = "No attachment",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit expense")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete expense")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ExpenseStatus) {
    val (containerColor, contentColor) = when (status) {
        ExpenseStatus.DRAFT -> MaterialTheme.colorScheme.surfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
        ExpenseStatus.FOR_REVIEW -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer
        ExpenseStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        ExpenseStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
        ExpenseStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseEditorScreen(
    initialDraft: ExpenseDraft,
    onCancel: () -> Unit,
    onSave: (Expense) -> Unit
) {
    val context = LocalContext.current
    var draft by remember(initialDraft.id) { mutableStateOf(initialDraft) }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                draft = draft.copy(
                    attachmentUri = uri.toString(),
                    attachmentName = displayNameForUri(context, uri) ?: "Attached bill"
                )
            }
        }
    )

    BackHandler(onBack = onCancel)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (initialDraft.id == null) "Add expense" else "Edit expense")
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val error = validateExpenseDraft(draft)
                            if (error == null) {
                                onSave(draft.toExpense())
                            } else {
                                validationMessage = error
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save expense")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = draft.vendor,
                    onValueChange = {
                        draft = draft.copy(vendor = it)
                        validationMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Vendor") },
                    leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.amount,
                    onValueChange = {
                        draft = draft.copy(amount = it)
                        validationMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Amount (INR)") },
                    leadingIcon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnumDropdownField(
                        label = "Category",
                        selected = draft.category,
                        options = ExpenseCategory.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(category = it) },
                        modifier = Modifier.weight(1f)
                    )
                    EnumDropdownField(
                        label = "Status",
                        selected = draft.status,
                        options = ExpenseStatus.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(status = it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.date,
                        onValueChange = {
                            draft = draft.copy(date = it)
                            validationMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Date") },
                        leadingIcon = {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null)
                        }
                    )
                    EnumDropdownField(
                        label = "Payment",
                        selected = draft.paymentMethod,
                        options = PaymentMethod.entries,
                        optionLabel = { it.label },
                        onSelected = { draft = draft.copy(paymentMethod = it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = draft.submittedBy,
                    onValueChange = {
                        draft = draft.copy(submittedBy = it)
                        validationMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Submitted by") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.invoiceNumber,
                    onValueChange = { draft = draft.copy(invoiceNumber = it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Invoice number") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                    }
                )
            }
            item {
                AttachmentField(
                    attachmentName = draft.attachmentName,
                    onPick = { documentPicker.launch(arrayOf("application/pdf", "image/*")) },
                    onClear = {
                        draft = draft.copy(attachmentUri = null, attachmentName = null)
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = { draft = draft.copy(notes = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp),
                    label = { Text("Notes") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) }
                )
            }
            validationMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val error = validateExpenseDraft(draft)
                            if (error == null) {
                                onSave(draft.toExpense())
                            } else {
                                validationMessage = error
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentField(
    attachmentName: String?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Bill or invoice",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = attachmentName ?: "No file selected",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (attachmentName != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Outlined.Close, contentDescription = "Remove attachment")
                }
            }
            OutlinedButton(onClick = onPick) {
                Text(if (attachmentName == null) "Attach" else "Replace")
            }
        }
    }
}

@Composable
private fun <T : Enum<T>> EnumDropdownField(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = optionLabel(selected),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Outlined.ExpandMore, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class ExpenseSummary(
    val totalSpend: Double,
    val thisMonthSpend: Double,
    val paidSpend: Double,
    val pendingCount: Int,
    val topCategory: String
) {
    companion object {
        fun from(expenses: List<Expense>): ExpenseSummary {
            val currentMonth = YearMonth.now()
            val topCategory = expenses
                .groupBy { it.category }
                .maxByOrNull { (_, categoryExpenses) -> categoryExpenses.sumOf { it.amount } }
                ?.key
                ?.label ?: "None"

            return ExpenseSummary(
                totalSpend = expenses.sumOf { it.amount },
                thisMonthSpend = expenses
                    .filter { parsedYearMonth(it.date) == currentMonth }
                    .sumOf { it.amount },
                paidSpend = expenses
                    .filter { it.status == ExpenseStatus.PAID }
                    .sumOf { it.amount },
                pendingCount = expenses.count {
                    it.status == ExpenseStatus.DRAFT || it.status == ExpenseStatus.FOR_REVIEW
                },
                topCategory = topCategory
            )
        }
    }
}

private fun sortedExpenses(expenses: List<Expense>): List<Expense> =
    expenses.sortedWith(
        compareByDescending<Expense> { parsedDate(it.date) ?: LocalDate.MIN }
            .thenByDescending { it.updatedAt }
    )

private fun parsedDate(date: String): LocalDate? =
    runCatching { LocalDate.parse(date) }.getOrNull()

private fun parsedYearMonth(date: String): YearMonth? =
    parsedDate(date)?.let { YearMonth.from(it) }

private fun formatExpenseDate(date: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    return parsedDate(date)?.format(formatter) ?: date
}

private fun displayNameForUri(context: Context, uri: Uri): String? =
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }

private fun openAttachment(context: Context, attachmentUri: String?) {
    if (attachmentUri == null) return
    val uri = attachmentUri.toUri()
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "No app available to open this attachment.", Toast.LENGTH_SHORT)
            .show()
    }
}

private fun saveExportToUri(context: Context, uri: Uri?, export: ExpenseExport) {
    if (uri == null) return
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(export.content.toByteArray(Charsets.UTF_8))
        } ?: error("Unable to open selected file.")
    }.onSuccess {
        Toast.makeText(context, "Exported ${export.fileName}", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "Could not export records.", Toast.LENGTH_SHORT).show()
    }
}
